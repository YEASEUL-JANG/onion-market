package com.onion.backend.dto;

public class LoginResponse {
    //token 필드를 private으로 설정하여 외부접근을 막고, getter 메서드를 통해서만 값을 읽을 수 있게 설정
    private String token;
    public LoginResponse(String token){
        this.token = token;
    }
    //Getter
    public String getToken(){
        return token;
    }
}
