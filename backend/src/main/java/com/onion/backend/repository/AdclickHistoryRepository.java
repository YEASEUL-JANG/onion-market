package com.onion.backend.repository;


import com.onion.backend.entity.AdClickHistory;
import com.onion.backend.entity.AdViewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdclickHistoryRepository extends MongoRepository<AdClickHistory, String> {
}