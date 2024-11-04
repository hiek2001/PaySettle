package com.project.revenueservice.client;

import com.project.revenueservice.dto.AdCountBatchDto;
import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import com.project.revenueservice.dto.VideoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
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

    @GetMapping("/ad-batch/count-by-date")
    List<AdCountBatchDto> getAdCountByDate(@RequestParam("currentDate") LocalDate currentDate);

}
