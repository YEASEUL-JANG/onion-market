package com.onion.backend.dto;

import lombok.Getter;

@Getter
public class SignUpReq {
    private String username;
    private String password;
    private String email;
}
