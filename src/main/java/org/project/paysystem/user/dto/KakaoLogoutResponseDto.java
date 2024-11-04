package org.project.paysystem.user.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class KakaoLogoutResponseDto {
    private HttpStatusCode status;
    private String message;
    private String data;


    @Builder
    public KakaoLogoutResponseDto(HttpStatusCode status, String message, String data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
