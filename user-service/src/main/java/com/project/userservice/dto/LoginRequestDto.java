package com.project.userservice.dto;

import lombok.Getter;

@Getter
public class LoginRequestDto {
    private Long socialId;
    private String email;
    private String username;
    private String password;


    public void setLoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
