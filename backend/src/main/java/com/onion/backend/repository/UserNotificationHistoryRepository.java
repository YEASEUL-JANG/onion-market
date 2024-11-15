package com.onion.backend.repository;


import com.onion.backend.entity.UserNotificationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserNotificationHistoryRepository extends MongoRepository<UserNotificationHistory, String> {
    List<UserNotificationHistory> findByUserIdAndCreatedDateAfter(Long userId, LocalDateTime date);
}