package com.onion.backend.repository;


import com.onion.backend.entity.AdViewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdviewHistoryRepository extends MongoRepository<AdViewHistory, String> {
}