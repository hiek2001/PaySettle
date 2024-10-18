package org.project.paysystem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.KakaoResponseDto;
import org.project.paysystem.dto.LoginRequestDto;
import org.project.paysystem.service.KakaoService;
import org.project.paysystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

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

    @Value("${kakao.api.client-id}")
    private String kakaoClientId;

    @Value("${url.base}")
    private String baseUrl;


    @Operation(summary = "카카오 로그인", description = "가입자 라면 로그인 되고, 미가입자 라면 회원가입이 됩니다.")
    @GetMapping("/login")
    public void kakaoLogin(@RequestParam String role ,HttpServletResponse response) throws IOException {
        StringBuilder loginUrl = new StringBuilder()
                .append(kakaoLoginUrl)
                .append("?client_id=").append(kakaoClientId)
                .append("&redirect_uri=").append(baseUrl).append(kakaoRedirectUri)
                .append("&response_type=code");

        if(Objects.equals(role, "seller")) loginUrl.append("&state=").append(role);

        response.sendRedirect(loginUrl.toString());
    }


    @Operation(summary = "카카오 로그인 Redirect URL")
    @GetMapping("/login/kakao/redirect")
    public ResponseEntity redirectKakaoLogin(@RequestParam String code, @RequestParam String state) throws JsonProcessingException {
        String token = kakaoService.redirectLogin(code, state);

        return ResponseEntity.ok(token);
    }

    @Operation(summary = "카카오 로그아웃" , description = "카카오와 함께 로그아웃 됩니다.")
    @GetMapping("/logout")
    public KakaoResponseDto logout(@RequestParam String accessToken) {
        return kakaoService.redirectLogout(accessToken);
    }

}
