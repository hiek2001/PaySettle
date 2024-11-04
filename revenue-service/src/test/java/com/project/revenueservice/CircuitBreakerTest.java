package com.project.revenueservice;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.AdCountBatchDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
public class CircuitBreakerTest {

    @Autowired
    private StreamingServiceClient streamingServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    LocalDate currentDate = LocalDate.parse("2024-11-03");

    @Test
    public void 정상_호출_테스트() {
        List<AdCountBatchDto> response = streamingServiceClient.getAdCountByDate(currentDate);
        assertThat(response).isNotEmpty(); // 정상 응답이 있을 경우에 대한 검증
    }

    @Test
    public void 서킷_브레이커_테스트_지연_호출() {
        try {
            // 실제 API 호출
            List<AdCountBatchDto> response = streamingServiceClient.getAdCountByDate(currentDate);

        } catch (Exception e) {
            // 예외 발생 시 fallback으로 빈 리스트가 반환되는지 확인
            List<AdCountBatchDto> response = streamingServiceClient.getAdCountByDate(currentDate);
            assertThat(response).isEmpty(); // fallback이 정상적으로 호출되었는지 검증
        }
    }

    @Test
    public void 서킷_브레이커_테스트_최소_요청수_호출() {
        // 서킷 브레이커가 열리도록 여러 번 실패 호출 발생
        for (int i = 0; i < 3; i++) {
            try {
                streamingServiceClient.getAdCountByDate(currentDate);
            } catch (Exception e) {
                // 예외 발생을 통해 실패율 증가
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("streamingCircuitBreaker");
                System.out.println("CircuitBreaker State: " + circuitBreaker.getState()); // OPEN인지 확인

                // 서킷 브레이커가 열린 상태에서 호출하여 fallback 호출 확인
                List<AdCountBatchDto> response = streamingServiceClient.getAdCountByDate(currentDate);

                // fallback 메서드가 호출되어 빈 리스트가 반환되는지 확인
                assertThat(response).isEmpty();
            }
        }
    }

    // todo: 재시도 및 스킵 설정 후 테스트 코드 작성 예정
}
