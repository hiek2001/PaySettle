package org.project.paysystem.revenue.dto;

import lombok.Builder;
import lombok.Getter;
import org.project.paysystem.streaming.entity.Video;

@Getter
public class RankVideoInfoDto {
    private int rank = 0;
    private Video video;
    private long totalViews;
    private long totalWatchTime;

    // setter
    public void updateRank(int rank) {
        this.rank = rank;
    }

    public RankVideoInfoDto(int rank, Video video, long totalViews, long totalWatchTime) {
        this.rank = rank;
        this.video = video;
        this.totalViews = totalViews;
        this.totalWatchTime = totalWatchTime;
    }


    @Builder
    public RankVideoInfoDto(Video video, long totalViews, long totalWatchTime) {
        this.totalViews = totalViews;
        this.video = video;
        this.totalWatchTime = totalWatchTime;
    }


}
