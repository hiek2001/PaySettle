package org.project.paysystem.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
@Builder
public class KakaoResponseDto {
    private HttpStatusCode status;
    private String message;
}
