package com.project.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    private final List<String> publicPaths = Arrays.asList(
            "/api/users/**","/api/streaming/**"
    );

    private final List<String> internalPaths = Arrays.asList(
            "/internal/"  // 내부 호출 전용 패턴
    );

    public JwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 인증 예외 경로인지 확인
            String path = request.getURI().getPath();
            log.info("JwtFilter is applied for path: {}", request.getURI().getPath());
            if (publicPaths.contains(path) || isInternalPath(path)) {
                return chain.filter(exchange); // 예외 경로는 필터 통과
            }

            // Authorization 헤더가 없으면 Unauthorized 처리
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return handleUnauthorized(response, "Missing Authorization header.");
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
            if (!authHeader.startsWith("Bearer ")) {
                return handleUnauthorized(response, "Invalid Authorization header format.");
            }

            String token = authHeader.substring(7);
            log.info("token {}", token);

            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(jwtSecret)
                        .parseClaimsJws(token)
                        .getBody();

                log.info("JWT Claims: ");
                ServerHttpRequest.Builder mutatedRequest = request.mutate();
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    String claimKey = "X-Claim-" + entry.getKey();
                    String claimValue = String.valueOf(entry.getValue());
                    mutatedRequest.header(claimKey, claimValue);
                    log.info("{}: {}", claimKey, claimValue);
                }

                request = mutatedRequest.build();
                exchange = exchange.mutate().request(request).build();

            } catch (Exception e) {
                log.error("JWT validation failed", e);
                return handleUnauthorized(response, "JWT validation failed: " + e.getMessage());
            }

            log.info("Custom PRE filter: request uri -> {}", request.getURI());
            log.info("Custom PRE filter: request id -> {}", request.getId());

            // chain.filter 호출 전 상태 코드 확인
            log.info("Status code before chain.filter: {}", response.getStatusCode());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                log.info("Custom POST filter: response status code -> {}", response.getStatusCode());
            }));
        };
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", HttpStatus.UNAUTHORIZED.getReasonPhrase(), message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Data
    public static class Config {
        private boolean preLogger;
        private boolean postLogger;
    }
    // 내부 호출 확인 메서드
    private boolean isInternalPath(String path) {
        return internalPaths.stream().anyMatch(path::contains);
    }

}
