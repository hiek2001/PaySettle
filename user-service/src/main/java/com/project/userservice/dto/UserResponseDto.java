package com.project.userservice.dto;


import com.project.userservice.entity.User;
import com.project.userservice.entity.UserRoleEnum;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final Long id;
//    private final String username;
//    private final String email;
//    private final UserRoleEnum role;

    public UserResponseDto(User user) {
        this.id = user.getId();
//        this.username = user.getUsername();
//        this.email = user.getEmail();
//        this.role = user.getRole();
    }
}
