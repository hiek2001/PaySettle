package com.project.streamingservice.controller;

import com.project.streamingservice.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/streaming/ads")
public class AdController {

    private final StreamingService streamingService;

    @GetMapping("/{videoId}")
    public void getAdAfterWatchTime(@PathVariable Long videoId) {
        streamingService.createVideoAdHistory(videoId);
    }
}
