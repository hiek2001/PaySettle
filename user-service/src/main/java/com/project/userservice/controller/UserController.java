package com.project.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.common.exception.RestApiException;
import com.project.userservice.dto.KakaoLoginResponseDto;
import com.project.userservice.dto.KakaoLogoutResponseDto;
import com.project.userservice.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j()
@RestController
@RequiredArgsConstructor
@Tag(name = "회원 API", description = "카카오 회원가입, 로그인, 로그아웃")
@RequestMapping("/api/users")
public class UserController {

    private final KakaoService kakaoService;

    @Value("${kakao.api.url.login}")
    private String kakaoLoginUrl;

    @Value("${kakao.api.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao_api_client_id}")
    private String kakaoClientId;

    @Value("${url.base}")
    private String baseUrl;



    @Operation(summary = "카카오 로그인", description = "가입자 라면 로그인 되고, 미가입자 라면 회원가입이 됩니다.")
    @GetMapping("/login/kakao")
    public void kakaoLogin(@RequestParam String role ,HttpServletResponse response) throws IOException {
        StringBuilder loginUrl = new StringBuilder()
                .append(kakaoLoginUrl)
                .append("?client_id=").append(kakaoClientId)
                .append("&redirect_uri=").append(baseUrl).append(kakaoRedirectUri)
                .append("&response_type=code")
                .append("&state=").append(role);
        ;

        response.sendRedirect(loginUrl.toString());
    }


    @Operation(summary = "카카오 로그인 Redirect URL", responses = {
            @ApiResponse(responseCode = "200", description = "로그인 및 회원가입 성공", content = @Content(schema = @Schema(implementation = KakaoLoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = RestApiException.class)))
    })
    @GetMapping("/login/kakao/redirect")
    public KakaoLoginResponseDto redirectKakaoLogin(@RequestParam String code, @RequestParam String state) throws JsonProcessingException {
        return kakaoService.redirectLogin(code, state);
    }

    @Operation(summary = "카카오 로그아웃" , description = "로그아웃 됩니다.", responses = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(implementation = KakaoLogoutResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = RestApiException.class)))
    })
    @GetMapping("/logout/kakao")
    public KakaoLogoutResponseDto logout(@RequestParam("kakaoToken") String kakaoToken) {
        return kakaoService.redirectLogout(kakaoToken);
    }

}
