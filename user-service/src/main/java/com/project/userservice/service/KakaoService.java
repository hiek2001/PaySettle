package com.project.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.userservice.dto.KakaoLoginResponseDto;
import com.project.userservice.dto.KakaoLogoutResponseDto;
import com.project.userservice.dto.KakaoUserInfoDto;
import com.project.userservice.entity.PlatformEnum;
import com.project.userservice.entity.SocialUser;
import com.project.userservice.entity.User;
import com.project.userservice.entity.UserRoleEnum;
import com.project.userservice.external.kakao.KakaoApiClient;
import com.project.userservice.external.kakao.KakaoAuthClient;
import com.project.userservice.repository.SocialUserRepository;
import com.project.userservice.repository.UserRepository;
import com.project.userservice.security.JwtUtil;
import com.project.userservice.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j(topic = "KAKAO LOGIN")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final JwtUtil jwtUtil;
    private final KakaoApiClient apiClient;
    private final KakaoAuthClient authClient;

    private final UserRepository userRepository;
    private final SocialUserRepository socialUserRepository;

    @Value("${kakao_api_client_id}")
    private String clientId;

    @Value("${kakao.api.redirect-uri}")
    private String redirectUri;

    @Value("${url.base}")
    private String baseUrl;


    public KakaoLoginResponseDto redirectLogin(String code, String role) throws JsonProcessingException {
        String accessToken = getToken(code);
        KakaoUserInfoDto kakaoUserInfo = getUserInfo(accessToken);
        User kakaoUser = registerUserIfNeeded(kakaoUserInfo, role);
        UserDetailsImpl userDetails = new UserDetailsImpl(kakaoUser);

        String token = jwtUtil.createToken(userDetails.getUsername(), userDetails.getUser().getRole()); // 메소드명은 username이지만 실제로는 email을 반환

        return KakaoLoginResponseDto.builder()
                .status(HttpStatus.OK)
                .message("Kakao Login Success")
                .email(userDetails.getUsername())
                .role(userDetails.getUser().getRole())
                .kakaoToken(accessToken)
                .token(token)
                .build();
    }

    private KakaoUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {

        String authorizationHeader = "Bearer " + accessToken;
        String response = apiClient.getUserInfo(authorizationHeader);

        JsonNode jsonNode = new ObjectMapper().readTree(response);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + id + ", " + nickname+" , "+email);
        return KakaoUserInfoDto.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .build();
    }

    private String getToken(String code) throws JsonProcessingException {
        log.info("인가코드 : "+code);

        String response = authClient.getTokenInfo("authorization_code", clientId, baseUrl + redirectUri, code);

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        return jsonNode.get("access_token").asText();
    }

    // 애초에 여기는 카카오 서비스이기 때문에 카카오만 처리
    // 다른 소셜은 다른 서비스에서 처리할 예정
    private User registerUserIfNeeded(KakaoUserInfoDto kakaoUserInfo, String role) {
        User kakaoUser = new User();

        // DB에 중복된 Kakao Id가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        SocialUser socialUser = socialUserRepository.findByKakaoId(kakaoId).orElse(null);
        if(socialUser == null) { // 들어온 값으로 소셜 로그인 가입, 우선 카카오만 있기 때문에 이 기준으로만 가입 시키기
            // socialUser에 먼저 저장
            socialUser = SocialUser.builder()
                    .kakaoId(kakaoId)
                    .platformType(PlatformEnum.KAKAO)
                    .build();

            socialUserRepository.save(socialUser);

            // 신규 회원 가입
            String email = kakaoUserInfo.getEmail();

            if(Objects.equals(role, "seller")) {
                    kakaoUser = User.builder()
                            .username(kakaoUserInfo.getNickname())
                            .email(email)
                            .role(UserRoleEnum.SELLER)
                            .socialUser(socialUser)
                            .build();
                } else {
                    kakaoUser = User.builder()
                            .username(kakaoUserInfo.getNickname())
                            .email(email)
                            .role(UserRoleEnum.USER)
                            .socialUser(socialUser)
                            .build();
                }

            userRepository.save(kakaoUser);
        }

       kakaoUser = userRepository.findBySocialUser(socialUser.getId());

        return kakaoUser;
    }

    public KakaoLogoutResponseDto redirectLogout(String kakaoToken) {
        String authorizationHeader = "Bearer " + kakaoToken;
        String response = apiClient.logout(authorizationHeader);

        return KakaoLogoutResponseDto.builder()
                .status(HttpStatus.OK)
                .message("Kakao Logout Success")
                .data(response)
                .build();
    }
}
