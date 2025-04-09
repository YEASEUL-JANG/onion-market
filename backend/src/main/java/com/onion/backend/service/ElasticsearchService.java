package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.ArticleResDto;
import com.onion.backend.entity.Article;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ElasticsearchService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ElasticsearchService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }
    //Mono : 주로 비동기적인 HTTP 응답이나 데이터베이스 조회와 같은 작업에서 사용

    /**
     * Elasticsearch에 Article 인덱스 생성 (한글분석기, 샤드 설정 포함)
     */
    public Mono<String> createArticleIndexIfNotExists() {
        return webClient.head()
                .uri("/article")
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> {
                    // 인덱스가 존재하면 그냥 메시지 반환
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Mono.just("Index가 이미 존재합니다.");
                    }
                    return createArticleIndexWithSettings(); // 없으면 새로 생성
                })
                .onErrorResume(error -> {
                    // 404 에러일 경우 = 인덱스 없음 → 생성
                    return createArticleIndexWithSettings();
                });
    }

    public Mono<String> createArticleIndexWithSettings() {
        String createIndexPayload = """
    {
      "settings": {
        "number_of_shards": 3,
        "number_of_replicas": 1,
        "analysis": {
          "analyzer": {
            "nori_analyzer": {
              "type": "custom",
              "tokenizer": "nori_tokenizer"
            }
          }
        }
      },
      "mappings": {
        "properties": {
          "title": {
            "type": "text",
            "analyzer": "nori_analyzer",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "content": {
            "type": "text",
            "analyzer": "nori_analyzer"
          },
          "created_date": {
            "type": "date",
            "format": "yyyy-MM-dd'T'HH:mm:ss.SSS"
          },
          "updated_date": {
            "type": "date",
            "format": "yyyy-MM-dd'T'HH:mm:ss.SSS"
          },
          "author_id": {
            "type": "long"
          },
          "board_id": {
            "type": "long"
          },
          "author_name": {
            "type": "text",
            "analyzer": "nori_analyzer"
          },
          "is_deleted": {
            "type": "boolean"
          }
        }
      }
    }
    """;

        return webClient.put()
                .uri("/article")
                .header("Content-Type", "application/json")
                .bodyValue(createIndexPayload)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> searchArticleIdsByKeyword(String keyword) {
        String searchQuery = """
        {
          "_source": false,
          "query": {
              "multi_match": {
                  "query": "%s",
                  "fields": [ "title^2", "content" ]
              }
         }
        }
        """.formatted(keyword, keyword);

        return webClient.post()
                .uri("/article/_search")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue(searchQuery)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> indexArticleDocument(String articleId, Article article) {
        try{
            return webClient.put()
                    .uri("/article/_doc/" + articleId)
                    .header("Content-Type","application/json")
                    .header("Accept","application/json")
                    .bodyValue(objectMapper.writeValueAsString(article))
                    .retrieve()
                    .bodyToMono(String.class);
        }catch (Exception e){
            e.printStackTrace();
            return Mono.error(new RuntimeException("Failed to index document"));
        }
    }
    /**
     * Elasticsearch에서 Article 조회
     */
    public Mono<ArticleResDto> getArticleById(Long articleId) {
        String endpoint = "/article/_doc/" + articleId;
        return webClient.get()
                .uri(endpoint)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode root = objectMapper.readTree(response);
                        JsonNode source = root.get("_source");
                        if (source != null) {
                            ArticleResDto article = objectMapper.treeToValue(source, ArticleResDto.class);
                            return Mono.just(article);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // 로깅
                    }
                    return Mono.empty();
                });
    }
}
