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

    public Mono<String> searchArticleIdsByKeyword(String keyword) {
        String searchQuery = """
        {
          "_source": false,
          "query": {
            "bool": {
              "should": [
                { "match": { "title": "%s" }},
                { "match": { "content": "%s" }}
              ]
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
