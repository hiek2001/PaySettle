package com.project.streamingservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoDto {
    private Long id;
    private Long videoViews;

    @Builder
    public VideoDto(Long id, Long videoViews) {
        this.id = id;
        this.videoViews = videoViews;
    }
}
