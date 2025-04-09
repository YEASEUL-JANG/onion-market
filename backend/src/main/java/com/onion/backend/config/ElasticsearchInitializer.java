package com.onion.backend.config;

import com.onion.backend.service.ElasticsearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchInitializer implements CommandLineRunner {
    private final ElasticsearchService elasticsearchService;

    public ElasticsearchInitializer(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public void run(String... args) {
        elasticsearchService.createArticleIndexIfNotExists()
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }
}
