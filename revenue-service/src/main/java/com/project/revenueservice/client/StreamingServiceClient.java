package com.project.revenueservice.client;

import com.project.revenueservice.dto.AdCountBatchDto;
import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import com.project.revenueservice.dto.VideoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@FeignClient(name = "streamingServiceClient", url = "http://localhost:9003/api/streaming")
public interface StreamingServiceClient {

    @GetMapping("/videos/all")
    List<VideoDto> getAllVideos();

    @GetMapping("/videos/all/Id")
    List<Long> getAllVideoIds();

    @GetMapping("/videos/history/watch-time/today")
    List<UserVideoHistoryBatchDto> getVideoByDay();

    @GetMapping("/videos/history/latest-watch")
    List<Long> getLatestVideos();

    @GetMapping("/videos/history/total-watch-time")
    List<UserVideoHistoryBatchDto> getTotalWatchTimeByVideo();

    @CircuitBreaker(name="streamingCircuitBreaker", fallbackMethod = "timeoutFallback")
    @GetMapping("/ad-batch/count-by-date")
    List<AdCountBatchDto> getAdCountByDate(@RequestParam("currentDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate);
    // todo: 재시도와 스킵 기능을 활용해 일시적인 네트워크 문제나 서비스 장애로 인한 API 호출 실패를 자동으로 재시도 할 수 있도록 로직 추가 예정

    // Fallback 메서드 추가
    default List<AdCountBatchDto> timeoutFallback(LocalDate currentDate, Throwable t) {
        return Collections.emptyList();
    }
}
