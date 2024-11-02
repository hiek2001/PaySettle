package com.project.revenueservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class VideoDto implements Serializable {
    private Long id;
    private Long duration;
    private Long views;
    private String videoUrl;

    @Builder
    public VideoDto(Long id, Long duration, Long views, String videoUrl) {
        this.id = id;
        this.duration = duration;
        this.views = views;
        this.videoUrl = videoUrl;

    }
}
