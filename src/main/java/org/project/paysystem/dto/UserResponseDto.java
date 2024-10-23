package org.project.paysystem.dto;


import lombok.Getter;
import org.project.paysystem.entity.User;
import org.project.paysystem.entity.UserRoleEnum;

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
