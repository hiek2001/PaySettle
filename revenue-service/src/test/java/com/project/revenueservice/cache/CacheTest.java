package com.project.revenueservice.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.revenueservice.dto.RevenueInfoDto;
import com.project.revenueservice.dto.RevenueInfoListWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Redis Read Test")
@SpringBootTest
public class CacheTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "revenue:test:2024-11-15";

    @BeforeEach
    public void setup() {
        // 테스트 시작 전에 Redis 캐시 초기화
        redisTemplate.delete(CACHE_KEY);
    }

    @DisplayName("Redis에 list를 저장하면 정상적으로 조회되어야 한다")
    @Test
    public void testCacheStorage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 1. 캐시할 데이터 준비
        RevenueInfoListWrapper list = new RevenueInfoListWrapper();
        RevenueInfoDto dto = new RevenueInfoDto(1L, 50001);
        RevenueInfoDto dto2 = new RevenueInfoDto(2L, 50001);
        list.addList(dto);
        list.addList(dto2);

        // 2. 캐시에 데이터 저장
        redisTemplate.opsForValue().set(CACHE_KEY, list);

        // 3. 캐시에 저장된 데이터 조회
        RevenueInfoListWrapper cache = (RevenueInfoListWrapper) redisTemplate.opsForValue().get(CACHE_KEY);

        // 4. 저장된 데이터가 예상한 값인지 검증
        assertThat(cache).isNotNull();
        assertThat(cache.getRevenues().size()).isEqualTo(2);
        assertThat(cache.getRevenues().get(0).getVideoId()).isEqualTo(1L);
        assertThat(cache.getRevenues().get(0).getAmount()).isEqualTo(50001);
        assertThat(cache.getRevenues().get(1).getVideoId()).isEqualTo(2L);
        assertThat(cache.getRevenues().get(1).getAmount()).isEqualTo(50001);
    }
}
