package org.project.paysystem.dto;

import lombok.Getter;

@Getter
public class VideoDailyStatusBatchDto {
    private Long videoId;
    private Long dailyViews;
    private Long dailyWatchTime;

    public VideoDailyStatusBatchDto(Long videoId, Long dailyViews, Long dailyWatchTime) {
        this.videoId = videoId;
        this.dailyViews = dailyViews;
        this.dailyWatchTime = dailyWatchTime;
    }
}
