package com.onion.backend.service;

import com.onion.backend.entity.Article;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ElasticsearchService {

    private final WebClient webClient;

    public ElasticsearchService(WebClient webClient) {
        this.webClient = webClient;
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

    public Mono<String> indexArticleDocument(String articleId, String jsonData) {
        return webClient.put()
                .uri("/article/_doc/" + articleId)
                .header("Content-Type","application/json")
                .header("Accept","application/json")
                .bodyValue(jsonData)
                .retrieve()
                .bodyToMono(String.class);
    }
}
