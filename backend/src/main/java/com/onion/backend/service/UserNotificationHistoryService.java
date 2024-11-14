package com.onion.backend.service;

import com.onion.backend.dto.AdViewHistoryResDto;
import com.onion.backend.dto.AdvertisementReqDto;
import com.onion.backend.dto.AdvertisementResDto;
import com.onion.backend.entity.*;
import com.onion.backend.repository.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserNotificationHistoryService {
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserNotificationHistoryService(MongoTemplate mongoTemplate, UserNotificationHistoryRepository userNotificationHistoryRepository) {
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
        this.mongoTemplate = mongoTemplate;
    }
    public void insertArticleNotification(Article article, Long userId){
    UserNotificationHistory history = new UserNotificationHistory();
        history.setTitle("글이 작성되었습니다");
        history.setContent(article.getTitle());
        history.setUserId(userId);
        userNotificationHistoryRepository.save(history);
    }

    public void insertCommentNotification(Comment comment, Long userId){
        UserNotificationHistory history = new UserNotificationHistory();
        history.setTitle("댓글이 작성되었습니다");
        history.setContent(comment.getContent());
        history.setUserId(userId);
        userNotificationHistoryRepository.save(history);
    }

    public void readNotification(String id){
        Optional<UserNotificationHistory> history = userNotificationHistoryRepository.findById(id);
        if(history.isEmpty()){
            return;
        }
        history.get().setIsRead(true);
        history.get().setUpdateDate(LocalDateTime.now());
        userNotificationHistoryRepository.save(history.get());
    }
}
