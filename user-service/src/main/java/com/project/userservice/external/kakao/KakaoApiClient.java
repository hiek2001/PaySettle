package com.project.userservice.external.kakao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoApiClient", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    @GetMapping("/v2/user/me")
    String getUserInfo(@RequestHeader("Authorization") String accessToken);

    @GetMapping("/v1/user/logout")
    String logout(@RequestHeader("Authorization") String accessToken);
}
