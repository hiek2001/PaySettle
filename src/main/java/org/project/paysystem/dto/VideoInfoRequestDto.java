package org.project.paysystem.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoInfoRequestDto {
    private Long duration;
    private String videoUrl;

    @Builder
    public VideoInfoRequestDto(Long duration, String videoUrl) {
        this.duration = duration;
        this.videoUrl = videoUrl;
    }
}
