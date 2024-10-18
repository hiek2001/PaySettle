package org.project.paysystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.UserHistoryResponseDto;
import org.project.paysystem.dto.VideoControlReqeustDto;
import org.project.paysystem.security.UserDetailsImpl;
import org.project.paysystem.service.UserVideoHistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "스트리밍 API", description = "동영상 상태 관리(페이지 이동, 재생 또는 정지, 재생 내역 저장)")
@RequestMapping("/api/streaming/video")
public class VideoController {

    private final UserVideoHistoryService userVideoHistoryService;

    @Operation(summary = "동영상 페이지 이동", description = "페이지 접속 시 영상이 자동 재생되며, 회원의 재생 내역을 저장")
    @PostMapping("/{videoId}")
    public UserHistoryResponseDto goPage(@PathVariable Long videoId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userVideoHistoryService.createHistory(videoId, userDetails.getUser());
    }

    @Operation(summary = "동영상 재생 또는 정지", description = "재생 또는 정지 시, 회원의 재생 상태와 내역 변경")
    @PostMapping("/{videoId}/playback")
    public void videoPlayOrPause(@PathVariable Long videoId, @RequestBody VideoControlReqeustDto reqeustDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userVideoHistoryService.updateVideoPlayback(videoId, reqeustDto, userDetails.getUser());
    }


}
