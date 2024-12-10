package com.onion.backend.service;

import com.onion.backend.dto.SignUpReq;
import com.onion.backend.dto.WriteDeviceDto;
import com.onion.backend.entity.Device;
import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<Device> getDevices(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails  =(UserDetails)  authentication.getPrincipal();
//        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
//        if (user.isPresent()){
//            return user.get().getDeviceList();
//        }else{
//            return new ArrayList<>();
//        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()
                -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        return user.getDeviceList();

    }

    public Device addDevice(WriteDeviceDto writeDeviceDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails  =(UserDetails)  authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if (user.isPresent()){
            Device device = new Device();
            device.setDeviceName(writeDeviceDto.getDeviceName());
            device.setToken(writeDeviceDto.getToken());
            user.get().getDeviceList().add(device);
            userRepository.save(user.get());
            return device;
        }
        return null;
    }
}
