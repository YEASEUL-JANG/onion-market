package com.onion.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        String basicAuthValue = "Basic " + Base64.getEncoder()
                .encodeToString(("elastic:onion1!").getBytes(StandardCharsets.UTF_8));

        return webClientBuilder
                .baseUrl("http://localhost:9200")  // Elasticsearch URL 설정
                .defaultHeader("Authorization", basicAuthValue)  // Basic Auth 헤더 추가
                .build();
    }
}
