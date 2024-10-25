package org.project.paysystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.paysystem.dto.KakaoLoginResponseDto;
import org.project.paysystem.dto.KakaoLogoutResponseDto;
import org.project.paysystem.dto.KakaoUserInfoDto;
import org.project.paysystem.entity.PlatformEnum;
import org.project.paysystem.entity.SocialUser;
import org.project.paysystem.entity.User;
import org.project.paysystem.entity.UserRoleEnum;
import org.project.paysystem.exception.KakaoApiException;
import org.project.paysystem.repository.SocialUserRepository;
import org.project.paysystem.repository.UserRepository;
import org.project.paysystem.security.UserDetailsImpl;
import org.project.paysystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

@Slf4j(topic = "KAKAO LOGIN")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final SocialUserRepository socialUserRepository;

    @Value("${kakao_api_client_id}")
    private String clientId;

    @Value("${kakao.api.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.api.url.profile}")
    private String profileUrl;

    @Value("${kakao.api.url.token}")
    private String tokenUrl;

    @Value("${url.base}")
    private String baseUrl;

    @Value("${kakao.api.url.logout}")
    private String kakaoLogoutUrl;

    private final MessageSource messageSource;

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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);
        log.info("accessToken : "+accessToken);

        if(profileUrl == null) {
            throw new KakaoApiException(messageSource.getMessage(
                    "redirect.uri.mismatch",
                    null,
                    "Redirect Url Mismatch",
                    Locale.getDefault()
            ));
        }
        URI uri = URI.create(profileUrl);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
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

        if (tokenUrl == null) throw new KakaoApiException(messageSource.getMessage(
                "redirect.uri.mismatch",
                null,
                "Redirect Url Mismatch",
                Locale.getDefault()
        ));
        URI requestUri = URI.create(tokenUrl);

        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","authorization_code");
        body.add("client_id",clientId);
        body.add("redirect_uri", baseUrl + redirectUri);
        body.add("code",code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(requestUri)
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.postForEntity(requestUri, requestEntity, String.class);

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
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
        if (kakaoLogoutUrl == null) throw new KakaoApiException(messageSource.getMessage(
                "redirect.uri.mismatch",
                null,
                "Redirect Url Mismatch",
                Locale.getDefault()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + kakaoToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.postForEntity(kakaoLogoutUrl, requestEntity, String.class);

        return KakaoLogoutResponseDto.builder()
                .status(HttpStatus.OK)
                .message("Kakao Logout Success")
                .data(response.getBody())
                .build();
    }
}
