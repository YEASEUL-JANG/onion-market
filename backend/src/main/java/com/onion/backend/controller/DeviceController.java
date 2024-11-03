package com.onion.backend.controller;

import com.onion.backend.dto.LoginReqForm;
import com.onion.backend.dto.SignUpReq;
import com.onion.backend.dto.TokenValidationRequest;
import com.onion.backend.dto.WriteDeviceDto;
import com.onion.backend.entity.Device;
import com.onion.backend.entity.User;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.service.CustomUserDetailsService;
import com.onion.backend.service.JwtBlacklistService;
import com.onion.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final UserService userService;
    @Autowired
    public DeviceController(UserService userService) {
        this.userService = userService;
    }


    //유저 기기 목록
    @GetMapping("")
    public ResponseEntity<List<Device>> getDevices(){
        return ResponseEntity.ok(userService.getDevices());
    }
    //유저 기기 생성
    @PostMapping("/add")
    public ResponseEntity<Device> addDevice(
            @RequestBody WriteDeviceDto writeDeviceDto){
        return ResponseEntity.ok(userService.addDevice(writeDeviceDto));
    }


}
