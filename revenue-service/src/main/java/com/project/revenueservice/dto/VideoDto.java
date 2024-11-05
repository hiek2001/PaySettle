package com.project.revenueservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class VideoDto implements Serializable {
    private Long id;
    private Long views;

    @Builder
    public VideoDto(Long id, Long views) {
        this.id = id;
        this.views = views;
    }
}
