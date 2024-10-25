package com.onion.backend.repository;


import com.onion.backend.entity.AdViewHistory;
import com.onion.backend.entity.AdViewStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdviewStatRepository extends JpaRepository<AdViewStat, Long> {
}