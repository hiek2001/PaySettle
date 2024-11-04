package com.project.streamingservice.dto;

import lombok.Getter;

@Getter
public class AdCountBatchDto {
    private Long videoId;
    private long adCount;

    public AdCountBatchDto(Long videoId, long adCount) {
        this.videoId = videoId;
        this.adCount = adCount;
    }
}
