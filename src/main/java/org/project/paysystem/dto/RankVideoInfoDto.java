package org.project.paysystem.dto;

import lombok.Builder;
import lombok.Getter;
import org.project.paysystem.entity.Video;

@Getter
public class RankVideoInfoDto {
    private int rank = 0;
    private Video video;
    private long totalViews;

    @Builder
    public RankVideoInfoDto(Video video, long totalViews) {
        this.totalViews = totalViews;
        this.video = video;
    }

    @Builder
    public RankVideoInfoDto(int rank, Video video, long totalViews) {
        this.rank = rank;
        this.totalViews = totalViews;
        this.video = video;
    }

    public void updateRank(int rank) {
        this.rank = rank;
    }

}
