package org.project.paysystem.revenue.dto;

import lombok.Getter;

@Getter
public class VideoDailyStatsBatchDto {
    private Long videoId;
    private Long dailyViews;
    private Long dailyWatchTime;

    public VideoDailyStatsBatchDto(Long videoId, Long dailyViews, Long dailyWatchTime) {
        this.videoId = videoId;
        this.dailyViews = dailyViews;
        this.dailyWatchTime = dailyWatchTime;
    }
}
