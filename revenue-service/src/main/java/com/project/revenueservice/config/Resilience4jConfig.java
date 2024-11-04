package com.project.revenueservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

//    @Bean
//    @Primary
//    public CircuitBreaker streamingCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry){
//        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
//                .failureRateThreshold(50)                           // 실패율 50% 가 넘으면 open 상태로 전환
//                .slowCallRateThreshold(70)                          // 지연 요청이 70% 이상이 되면 opne 상태로 전환
//                .slowCallDurationThreshold(Duration.ofSeconds(10))   // 지연 요청이 10초가 넘으면 slow call이라고 판단
//                .permittedNumberOfCallsInHalfOpenState(3)           // half_open 상태에서 open/closed 상태 전환 판단할 요청 개수
//                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(3)) // half_open 상태를 3초 유지
//                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 슬라이딩 윈도우의 요청 개수 기반 설정
//                .slidingWindowSize(10)                              // 슬라이딩 윈도우 크기 설정
//                .minimumNumberOfCalls(10)                           // 슬라이딩 윈도우 한 단위당 slow-call, failure-call이 계산되는 최소 요청 수
//                .waitDurationInOpenState(Duration.ofSeconds(10))    // open 상태에서 최소 10초가 지나고 요청이 오면 half_open 상태가 되도록 설정
//                .build();
//
//        return circuitBreakerRegistry.circuitBreaker("streamingCircuitBreaker", config);
//    }

    // 원할한 테스트를 위해 짧게 설정
    @Bean
    @Primary
    public CircuitBreaker streamingCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry){
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)                           // 실패율 50% 가 넘으면 open 상태로 전환
                .slowCallRateThreshold(60)                          // 지연 요청이 60% 이상이 되면 opne 상태로 전환
                .slowCallDurationThreshold(Duration.ofSeconds(1))   // 지연 요청이 5초가 넘으면 slow call이라고 판단
                .permittedNumberOfCallsInHalfOpenState(2)           // half_open 상태에서 open/closed 상태 전환 판단할 요청 개수
                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(2)) // half_open 상태를 3초 유지
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // 슬라이딩 윈도우의 요청 개수 기반 설정
                .slidingWindowSize(2)                              // 슬라이딩 윈도우 크기 설정
                .minimumNumberOfCalls(2)                           // 슬라이딩 윈도우 한 단위당 slow-call, failure-call이 계산되는 최소 요청 수
                .waitDurationInOpenState(Duration.ofSeconds(2))    // open 상태에서 최소 10초가 지나고 요청이 오면 half_open 상태가 되도록 설정
                .build();

        return circuitBreakerRegistry.circuitBreaker("streamingCircuitBreaker", config);
    }

}
