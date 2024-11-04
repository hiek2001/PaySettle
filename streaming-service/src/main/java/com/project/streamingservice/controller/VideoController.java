package com.project.streamingservice.controller;

import com.project.common.exception.RestApiException;
import com.project.streamingservice.dto.*;
import com.project.streamingservice.service.StreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "스트리밍 API", description = "동영상 상태 관리(동영상 조회, 등록, 재생 또는 정지, 재생 내역 저장)")
@RequestMapping("/api/streaming/videos")
public class VideoController {
    private final StreamingService streamingService;

    @Operation(summary = "동영상 조회", description = "id를 사용하여 동영상 재생 내역을 저장합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "영상 조회 성공", content = @Content(schema = @Schema(implementation = UserHistoryResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = RestApiException.class)))
    })
    @GetMapping("/{videoId}")
    public UserHistoryResponseDto getVideo(@PathVariable Long videoId, @RequestBody UserRequestDto user) {
        return streamingService.createUserVideoHistory(videoId, user);
    }

    @Operation(summary = "동영상 재생 또는 정지", description = "재생 또는 정지 시, 회원의 재생 상태와 내역을 변경합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "재생 또는 정지 성공", content = @Content(schema = @Schema(implementation = UserHistoryResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스 접근", content = @Content(schema = @Schema(implementation = RestApiException.class)))
    })
    @PostMapping("/{videoId}/playback")
    public UserHistoryResponseDto playBack(@PathVariable Long videoId, @RequestBody VideoControlReqeustDto reqeustDto) {
       return streamingService.updateVideoPlayback(videoId, reqeustDto);
    }


    // batch
    @GetMapping("/all")
    public List<VideoDto> getAllVideos() {
        return streamingService.getAllVideos();
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
