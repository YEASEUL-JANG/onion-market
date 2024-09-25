package com.onion.backend.controller;

import com.onion.backend.entity.User;
import com.onion.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    //유저 생성
    @PostMapping("/signUp")
    public ResponseEntity<User> createUser(@RequestParam("username") String username,
                                           @RequestParam("password") String password,
                                           @RequestParam("email") String email){
        User user = userService.createUser(username, password, email);
        return ResponseEntity.ok(user);
    }

    //유저 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("userId") Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
