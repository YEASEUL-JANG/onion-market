package com.onion.backend.service;

import com.onion.backend.entity.JwtBlacklist;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.repository.JwtBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtBlacklistService {


    private final JwtBlacklistRepository jwtBlacklistRepository;
 @Autowired
    public JwtBlacklistService(JwtBlacklistRepository jwtBlacklistRepository) {
     this.jwtBlacklistRepository = jwtBlacklistRepository;
 }

    // 토큰을 블랙리스트에 추가
    public void addTokenToBlacklist(String token, LocalDateTime expiry, String username) {
        JwtBlacklist jwtBlacklist = new JwtBlacklist(token, expiry,username);
        jwtBlacklistRepository.save(jwtBlacklist);
    }

    // 로그인 요청 토큰이 블랙리스트의 최근 만료시간보다 이후에 생성된 토큰인지 확인
    public boolean isTokenBlacklisted(String username, LocalDateTime tokenIssuedAtDateTime) {
        //블랙리스트에서 유저의 가장 최근 로그아웃 시점 조회
        Optional<JwtBlacklist> blacklistedToken = jwtBlacklistRepository.findTopByUsernameOrderByExpiry(username);
        if (!blacklistedToken.isPresent()){// 로그아웃 기록이 없으면 로그인 통과
            return false;
        }
        // 블랙리스트에 있는 로그아웃 시점 이후에 생성된 토큰이면 유효함
        return !blacklistedToken.get().getExpiry().isBefore(tokenIssuedAtDateTime);

    }
}
