package org.project.paysystem.dto;

import lombok.Builder;
import lombok.Getter;
import org.project.paysystem.entity.UserRoleEnum;
import org.springframework.http.HttpStatusCode;

@Getter
public class KakaoLoginResponseDto {
    private HttpStatusCode status;
    private String message;
    private String email;
    private UserRoleEnum role;
    private String kakaoToken; // kakao 토큰
    private String token; // 자체 토큰

    @Builder
    public KakaoLoginResponseDto(HttpStatusCode status, String message, String email, UserRoleEnum role, String kakaoToken, String token) {
        this.status = status;
        this.message = message;
        this.email = email;
        this.role = role;
        this.kakaoToken = kakaoToken;
        this.token = token;
    }

}
