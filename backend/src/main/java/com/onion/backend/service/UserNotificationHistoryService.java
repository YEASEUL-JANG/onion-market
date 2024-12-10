package com.onion.backend.service;

import com.onion.backend.dto.AdViewHistoryResDto;
import com.onion.backend.dto.AdvertisementReqDto;
import com.onion.backend.dto.AdvertisementResDto;
import com.onion.backend.entity.*;
import com.onion.backend.exception.ResourceNotFoundException;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserNotificationHistoryService {
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    public UserNotificationHistoryService(MongoTemplate mongoTemplate, UserNotificationHistoryRepository userNotificationHistoryRepository, UserRepository userRepository, NoticeRepository noticeRepository) {
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.noticeRepository = noticeRepository;
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
        userNotificationHistoryRepository.findById(id).ifPresent(history -> {
            history.setIsRead(true);
            history.setUpdatedDate(LocalDateTime.now());
            userNotificationHistoryRepository.save(history);
        });
    }
    public List<UserNotificationHistory> getNotificationList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("author not found"));


        LocalDateTime weekDate = LocalDateTime.now().minusWeeks(7);
        // 일주일 전 알림만 노출
        List<UserNotificationHistory> userNotificationHistoryList = userNotificationHistoryRepository
                .findByUserIdAndCreatedDateAfter(user.getId(), weekDate);

        // 일주일 전 공지만 추출
        List<Notice> notices = noticeRepository.findByCreatedDate(weekDate);

        // 유저한테 나갈 history 만들기
        Map<Long, UserNotificationHistory> historyMap = userNotificationHistoryList.stream()
                .filter(history -> history.getNoticeId() != null)
                .collect(Collectors.toMap(UserNotificationHistory::getNoticeId, history -> history));

        List<UserNotificationHistory> results = userNotificationHistoryList.stream()
                .filter(history -> history.getNoticeId() == null)
                .collect(Collectors.toList());
        notices.forEach(notice -> {
            UserNotificationHistory history = historyMap.get(notice.getId());
            if (history == null) {
                history = new UserNotificationHistory();
                history.setTitle("공지사항이 작성되었습니다.");
                history.setContent(notice.getTitle());
                history.setUserId(user.getId());
                history.setIsRead(false);
                history.setNoticeId(notice.getId());
                history.setCreatedDate(notice.getCreatedDate());
                history.setUpdatedDate(null);
            }
            results.add(history);
        });
        return results;
    }

}
