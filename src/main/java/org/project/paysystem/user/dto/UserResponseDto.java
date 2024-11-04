package org.project.paysystem.user.dto;


import lombok.Getter;
import org.project.paysystem.user.entity.User;
import org.project.paysystem.user.entity.UserRoleEnum;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String username;
    private final String email;
    private final UserRoleEnum role;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
