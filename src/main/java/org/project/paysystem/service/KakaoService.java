package org.project.paysystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.paysystem.dto.KakaoResponseDto;
import org.project.paysystem.dto.KakaoUserInfoDto;
import org.project.paysystem.entity.User;
import org.project.paysystem.entity.UserRoleEnum;
import org.project.paysystem.exception.CCommunicationException;
import org.project.paysystem.repository.UserRepository;
import org.project.paysystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@Slf4j(topic = "KAKAO LOGIN")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    @Value("${kakao.api.client-id}")
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


    public String redirectLogin(String code, String role) throws JsonProcessingException {
        String accessToken = getToken(code);
        KakaoUserInfoDto kakaoUserInfo = getUserInfo(accessToken);
        User kakaoUser = registerUserIfNeeded(kakaoUserInfo, role);

        return jwtUtil.createToken(kakaoUser.getUsername(), kakaoUser.getRole()); // token 반환
    }

    private KakaoUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);
        log.info("accessToken : "+accessToken);

        if (profileUrl == null) throw new CCommunicationException("Kakao profile URL is not configured");
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

        if (tokenUrl == null) throw new CCommunicationException("Kakao token URL is not configured");
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


    private User registerUserIfNeeded(KakaoUserInfoDto kakaoUserInfo, String role) {
        // DB에 중복된 Kakao Id가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null); // 없으면 null을 반환

        if(kakaoUser == null) {
            String kakaoEmail = kakaoUserInfo.getEmail();

            // 신규 회원가입
            // password : random UUID
            String password = UUID.randomUUID().toString();
            String encodePassword = passwordEncoder.encode(password);

            // email : kakao email
            String email = kakaoUserInfo.getEmail();

            if(Objects.equals(role, "seller")) {
                kakaoUser = User.builder()
                        .username(kakaoUserInfo.getNickname())
                        .password(encodePassword)
                        .email(email)
                        .role(UserRoleEnum.SELLER)
                        .kakaoId(kakaoId)
                        .build();
            }

            kakaoUser = User.builder()
                    .username(kakaoUserInfo.getNickname())
                    .password(encodePassword)
                    .email(email)
                    .role(UserRoleEnum.USER)
                    .kakaoId(kakaoId)
                    .build();


            userRepository.save(kakaoUser);
        }
        return kakaoUser;
    }

    public KakaoResponseDto redirectLogout(String accessToken) {
        if (kakaoLogoutUrl == null) throw new CCommunicationException("Kakao logout URL is not configured");

       // jwtUtil.validateToken(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.postForEntity(kakaoLogoutUrl, requestEntity, String.class);

        return KakaoResponseDto.builder()
                .status(response.getStatusCode())
                .message(response.getBody())
                .build();
    }
}