package com.project.revenueservice.client;

import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import com.project.revenueservice.dto.VideoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "streamingServiceClient", url = "http://localhost:9003/api/streaming/videos")
public interface StreamingServiceClient {

    @GetMapping("/all")
    List<VideoDto> getAllVideos();

    @GetMapping("/all/Id")
    List<Long> getAllVideoIds();

    @GetMapping("/history/watch-time/today")
    List<UserVideoHistoryBatchDto> getVideoByDay();

    @GetMapping("/history/latest-watch")
    List<Long> getLatestVideos();

    @GetMapping("/history/total-watch-time")
    List<UserVideoHistoryBatchDto> getTotalWatchTimeByVideo();
}
