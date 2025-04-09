package com.onion.backend.service;

import com.onion.backend.entity.JwtBlacklist;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.repository.JwtBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;


    // 토큰을 블랙리스트에 추가
    public void addTokenToBlacklist(String token, LocalDateTime logoutTime, String username) {
        // 키: BL:{username} / 값: 로그아웃 시각 / TTL: 30분 (이후 자동 삭제)
        String key = "BL:" + username;
        redisTemplate.opsForValue().set(key, logoutTime, Duration.ofMinutes(30));
    }

    // 로그인 요청 토큰이 블랙리스트의 최근 만료시간보다 이후에 생성된 토큰인지 확인
    public boolean isTokenBlacklisted(String username, LocalDateTime tokenIssuedAtDateTime) {
        String key = "BL:" + username;
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            return false;
        }
        LocalDateTime logoutTime = (LocalDateTime) stored;
        return !logoutTime.isBefore(tokenIssuedAtDateTime);
    }
}
