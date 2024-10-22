package org.project.paysystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.paysystem.service.StreamingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "광고 API", description = "광고 관리(광고 삽입)")
@RequestMapping("/api/streaming/ad")
public class AdController {

    private final StreamingService streamingService;

    @Operation(summary = "특정 영상에 광고 삽입", description = "정해진 시청 시간이 경과하면 해당 API가 호출됨")
    @GetMapping("/{videoId}")
    public void getAdAfterWatchTime(@PathVariable Long videoId) {
        streamingService.createVideoAdHistory(videoId);
    }
}
