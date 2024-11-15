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
        Optional<UserNotificationHistory> history = userNotificationHistoryRepository.findById(id);
        if(history.isEmpty()){
            return;
        }
        history.get().setIsRead(true);
        history.get().setUpdatedDate(LocalDateTime.now());
        userNotificationHistoryRepository.save(history.get());
    }
    public List<UserNotificationHistory> getNotificationList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }

        LocalDateTime weekDate = LocalDateTime.now().minusWeeks(7);
        // 일주일 전 알림만 노출
        List<UserNotificationHistory> userNotificationHistoryList
                = userNotificationHistoryRepository.findByUserIdAndCreatedDateAfter(
                user.get().getId(), weekDate);

        // 일주일 전 공지만 추출
        List<Notice> notices = noticeRepository.findByCreatedDate(weekDate);

        // 유저한테 나갈 history 만들기
        List<UserNotificationHistory> results = new ArrayList<>();
        HashMap<Long, UserNotificationHistory> hashMap = new HashMap<>();
        for (UserNotificationHistory history : userNotificationHistoryList) {
            if (history.getNoticeId() != null) {
                // 공지시항 히스토리만 추가(중복제거하기 위함)
                hashMap.put(history.getNoticeId(), history);
            } else {
                results.add(history);
            }
        }
        for (Notice notice : notices) {
            UserNotificationHistory history = hashMap.get(notice.getId());
            if (history != null) {
                results.add(history);
            } else {
                history = new UserNotificationHistory();
                history.setTitle("공지사항이 작성되었습니다.");
                history.setContent(notice.getTitle());
                history.setUserId(user.get().getId());
                history.setIsRead(false);
                history.setNoticeId(notice.getId());
                history.setCreatedDate(notice.getCreatedDate());
                history.setUpdatedDate(null);
                results.add(history);
            }
        }

        return results;
    }

}
