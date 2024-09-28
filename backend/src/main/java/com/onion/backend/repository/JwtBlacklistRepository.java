package com.onion.backend.repository;

import com.onion.backend.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
    // 토큰이 블랙리스트에 있는지 확인하는 메서드
    Optional<JwtBlacklist> findByToken(String token);
    //가장 마직막으로 요청된 '모든기기 로그아웃' 목록 추출
    @Query(value = "SELECT * FROM jwt_blacklist WHERE username = :username ORDER BY expiry LIMIT 1", nativeQuery = true)
    Optional<JwtBlacklist> findTopByUsernameOrderByExpiry(@Param("username") String username);
}