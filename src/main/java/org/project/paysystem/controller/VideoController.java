package org.project.paysystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.UserHistoryResponseDto;
import org.project.paysystem.dto.VideoControlReqeustDto;
import org.project.paysystem.security.UserDetailsImpl;
import org.project.paysystem.service.StreamingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "스트리밍 API", description = "동영상 상태 관리(동영상 조회, 등록, 재생 또는 정지, 재생 내역 저장)")
@RequestMapping("/api/streaming/videos")
public class VideoController {

    private final StreamingService streamingService;

    @Operation(summary = "동영상 조회", description = "영상이 자동 재생된다고 가정, 회원의 재생 내역을 저장")
    @GetMapping("/{videoId}")
    public UserHistoryResponseDto getVideo(@PathVariable Long videoId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return streamingService.createUserVideoHistory(videoId, userDetails.getUser());
    }

    @Operation(summary = "동영상 재생 또는 정지", description = "재생 또는 정지 시, 회원의 재생 상태와 내역 변경")
    @PostMapping("/{videoId}/playback")
    public void playBack(@PathVariable Long videoId, @RequestBody VideoControlReqeustDto reqeustDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        streamingService.updateVideoPlayback(videoId, reqeustDto, userDetails.getUser());
    }
}
