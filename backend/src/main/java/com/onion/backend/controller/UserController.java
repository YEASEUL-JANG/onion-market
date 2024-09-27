package com.onion.backend.controller;

import com.onion.backend.dto.LoginForm;
import com.onion.backend.dto.LoginResponse;
import com.onion.backend.dto.SignUpUser;
import com.onion.backend.dto.TokenValidationRequest;
import com.onion.backend.entity.User;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.service.CustomUserDetailsService;
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

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }


    //유저 생성
    @PostMapping("/signUp")
    public ResponseEntity<User> createUser(@RequestBody SignUpUser signUpUser){
        User user = userService.createUser(signUpUser);
        return ResponseEntity.ok(user);
    }
    //유저 삭제
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("userId") Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginForm loginForm, HttpServletResponse response){
        try{
        //사용자 인증처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginForm.getUsername(), loginForm.getPassword()));
        //인증 성공 시, UserDetails 객체를 통해 사용자 정보 가져옴
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // JWT 토큰 생성
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

            // JWT 토큰을 쿠키에 저장
            Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
            jwtCookie.setHttpOnly(true); // XSS 공격 방지 (JavaScript로 접근 불가능)
            jwtCookie.setSecure(true);   // HTTPS에서만 전송 (보안 강화)
            jwtCookie.setPath("/");      // 쿠키의 적용 범위 설정
            jwtCookie.setMaxAge(60 * 60); // 쿠키 유효기간 설정 (1시간)

            // 쿠키를 응답에 추가
            response.addCookie(jwtCookie);

            return ResponseEntity.ok("login success");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // JWT 토큰을 삭제하기 위한 쿠키 설정
        Cookie jwtCookie = new Cookie("jwtToken", null); // 쿠키 값을 null로 설정
        jwtCookie.setHttpOnly(true); // 동일한 설정으로 보안 유지
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");      // 동일한 Path
        jwtCookie.setMaxAge(0);      // 쿠키 만료시간을 0으로 설정하여 삭제

        // 쿠키를 응답에 추가 (쿠키 삭제)
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("logout success");
    }

    // 토큰 유효성검사
    @PostMapping("/token/validate")
    @ResponseStatus(HttpStatus.OK)
    public void jwtValidate(@RequestBody TokenValidationRequest request){
        String token = request.getToken();
        String username = request.getUsername();
        boolean isValid = jwtUtil.validateToken(token,username);
        if (!isValid){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"토큰이 유효하지 않습니다.");
        }
    }

}
