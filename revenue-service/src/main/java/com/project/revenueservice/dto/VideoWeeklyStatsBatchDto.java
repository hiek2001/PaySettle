package com.project.revenueservice.dto;

import lombok.Getter;

@Getter
public class VideoWeeklyStatsBatchDto {
    private Long videoId;
    private Long weeklyViews;
    private Long weeklyWatchTime;

    public VideoWeeklyStatsBatchDto(Long videoId, Long weeklyViews, Long weeklyWatchTime) {
        this.videoId = videoId;
        this.weeklyViews = weeklyViews;
        this.weeklyWatchTime = weeklyWatchTime;
    }
}
