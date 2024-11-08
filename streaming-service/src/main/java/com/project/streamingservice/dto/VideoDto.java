package com.project.streamingservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoDto {
    private Long id;
    private long videoViews;

    @Builder
    public VideoDto(Long id, long videoViews) {
        this.id = id;
        this.videoViews = videoViews;
    }
}
