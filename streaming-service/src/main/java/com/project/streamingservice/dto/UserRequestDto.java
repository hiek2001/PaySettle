package com.project.streamingservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserRequestDto {
    private Long id;
    private String username;
    private String email;
    private String role;

    @Builder
    public UserRequestDto(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
