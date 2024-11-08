package com.project.revenueservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class VideoDto implements Serializable {
    private Long id;
    private long videoViews;

    @Builder
    public VideoDto(Long id, long videoViews) {
        this.id = id;
        this.videoViews = videoViews;
    }
}
