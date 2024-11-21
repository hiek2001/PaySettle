package com.project.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.userservice.dto.KakaoLoginResponseDto;
import com.project.userservice.dto.KakaoLogoutResponseDto;
import com.project.userservice.service.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
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

    @GetMapping("/login/kakao/redirect")
    public KakaoLoginResponseDto redirectKakaoLogin(@RequestParam String code, @RequestParam String state) throws JsonProcessingException {
        return kakaoService.redirectLogin(code, state);
    }

    @GetMapping("/logout/kakao")
    public KakaoLogoutResponseDto logout(@RequestParam("kakaoToken") String kakaoToken) {
        return kakaoService.redirectLogout(kakaoToken);
    }

}
