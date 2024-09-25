package com.onion.backend.service;

import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    // 유저 생성 메서드
    @Transactional
    public User createUser(String username, String password, String email) {
        // 이미 존재하는 유저가 있는지 확인
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("유저가 이미 존재합니다.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이메일이 이미 존재합니다.");
        }

        // 새로운 유저 생성
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 보안 상 여기서 암호화를 적용해야 함
        user.setEmail(email);
        user.setLastLogin(null);  // 처음 가입시엔 로그인 기록 없음

        // 유저 저장
        return userRepository.save(user);
    }

    //유저 삭제
    public void deleteUser(Long userId){
        userRepository.deleteById(userId);
    }
}
