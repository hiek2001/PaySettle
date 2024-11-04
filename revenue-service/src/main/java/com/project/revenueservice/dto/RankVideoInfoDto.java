package com.project.revenueservice.dto;

import com.project.streamingservice.entity.Video;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RankVideoInfoDto {
    private int rank = 0;
    private Long videoId;
   // private Video video;
    private long totalViews;
    private long totalWatchTime;

    // setter
    public void updateRank(int rank) {
        this.rank = rank;
    }

    public RankVideoInfoDto(int rank, Long videoId, long totalViews, long totalWatchTime) {
        this.rank = rank;
        this.videoId = videoId;
        this.totalViews = totalViews;
        this.totalWatchTime = totalWatchTime;
    }


    @Builder
    public RankVideoInfoDto(Long videoId, long totalViews, long totalWatchTime) {
        this.totalViews = totalViews;
        this.videoId = videoId;
        this.totalWatchTime = totalWatchTime;
    }


}
