package com.project.streamingservice.controller;

import com.project.streamingservice.dto.*;
import com.project.streamingservice.service.StreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/streaming/videos")
public class VideoController {
    private final StreamingService streamingService;

    @GetMapping("/{videoId}")
    public UserHistoryResponseDto getVideo(@PathVariable Long videoId, @RequestBody UserRequestDto user) {
        return streamingService.createUserVideoHistory(videoId, user);
    }

    @PostMapping("/{videoId}/playback")
    public UserHistoryResponseDto playBack(@PathVariable Long videoId, @RequestBody VideoControlReqeustDto reqeustDto) {
       return streamingService.updateVideoPlayback(videoId, reqeustDto);
    }


    // batch
    @GetMapping("/all")
    public List<VideoDto> getAllVideos() {
        return streamingService.getAllVideos();
    }

    @GetMapping("/lastId")
    public List<VideoDto> getVideoAfterId(@RequestParam("lastId") long lastId, @RequestParam("pageSize") int pageSize) {
        return streamingService.getVideoAfterId(lastId, pageSize);
    }

    @GetMapping("/total-count")
    public int getVideoTotalCount() {
        return streamingService.getVideoTotalCount();
    }

    @GetMapping("/all/Id")
    public List<Long> getAllVideoIds() {
        return streamingService.getAllVideoIds();
    }

    @GetMapping("/history/watch-time/today")
    public List<UserVideoHistoryBatchDto> getVideoByDay() {
        return streamingService.getVideoByDay();
    }

    @GetMapping("/history/latest-watch")
    public List<Long> getLatestVideos() {
        return streamingService.getLatestVideos();
    }

    @GetMapping("/history/total-watch-time")
    public List<UserVideoHistoryBatchDto> getTotalWatchTimeByVideo() {
        return streamingService.getTotalWatchTimeByVideo();
    }
}
