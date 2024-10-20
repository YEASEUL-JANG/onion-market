package com.onion.backend.service;

import com.onion.backend.dto.SignUpReq;
import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    // 유저 생성 메서드
    @Transactional
    public User createUser(SignUpReq signUpUser) {
        // 이미 존재하는 유저가 있는지 확인
        if (userRepository.findByUsername(signUpUser.getUsername()).isPresent()) {
            throw new IllegalArgumentException("유저가 이미 존재합니다.");
        }

        if (userRepository.findByEmail(signUpUser.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 이미 존재합니다.");
        }

        // 새로운 유저 생성
        User user = new User();
        user.setUsername(signUpUser.getUsername());
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpUser.getPassword());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 저장

        user.setEmail(signUpUser.getEmail());
        user.setLastLogin(null);  // 처음 가입시엔 로그인 기록 없음

        // 유저 저장
        return userRepository.save(user);
    }

    //유저 삭제
    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }
}
