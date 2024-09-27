package com.onion.backend.dto;

import lombok.Getter;

@Getter
public class TokenValidationRequest {
    private String token;
    private String username;
    private TokenValidationRequest(){

    }
    public TokenValidationRequest(String token, String username) {
        this.token = token;
        this.username = username;
    }
}
