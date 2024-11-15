package com.onion.backend.repository;


import com.onion.backend.entity.UserNotificationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserNotificationHistoryRepository extends MongoRepository<UserNotificationHistory, String> {
}