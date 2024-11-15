package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.*;
import com.onion.backend.entity.*;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final UserNotificationHistoryRepository userNotificationHistoryRepository;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository, UserRepository userRepository, UserNotificationHistoryRepository userNotificationHistoryRepository) {

        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
        this.userNotificationHistoryRepository = userNotificationHistoryRepository;
    }

    public Notice writeNotice(WriteNoticeDto dto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if(user.isEmpty()){
            throw new ResourceNotFoundException("user not found");
        }
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setAuthor(user.get());
        notice.setAuthorName(user.get().getUsername());
        noticeRepository.save(notice);

        return notice;
    }
    //공지사항은 잘 안읽기 때문에 읽었을 때 mongoDB history에 업데이트
    public Notice getNotice(Long noticeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("user not found");
        }
        Optional<Notice> notice = noticeRepository.findById(noticeId);
        UserNotificationHistory userNotificationHistory = new UserNotificationHistory();
        userNotificationHistory.setTitle("공지사항이 작성되었습니다.");
        userNotificationHistory.setContent(notice.get().getTitle());
        userNotificationHistory.setUserId(user.get().getId());
        userNotificationHistory.setIsRead(true);
        userNotificationHistory.setNoticeId(noticeId);
        userNotificationHistory.setCreatedDate(notice.get().getCreatedDate());
        userNotificationHistory.setUpdatedDate(LocalDateTime.now());
        userNotificationHistoryRepository.save(userNotificationHistory);
        return notice.get();
    }
}
