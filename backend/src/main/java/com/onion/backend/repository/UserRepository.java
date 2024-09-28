package com.onion.backend.repository;


import com.onion.backend.entity.JwtBlacklist;
import com.onion.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 사용자명을 기반으로 유저 찾기 (Optional로 반환)
    Optional<User> findByUsername(String username);

    // 이메일을 기반으로 유저 찾기 (Optional로 반환)
    Optional<User> findByEmail(String email);
}
