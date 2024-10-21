package org.project.paysystem.dto;

import lombok.Getter;

@Getter
public class LoginRequestDto {
    private Long socialId;
    private String username;
    private String password;


    public void setLoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
