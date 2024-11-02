package com.project.streamingservice.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class UserVideoHistoryBatchDto implements Serializable {
    private Long videoId;
    private Long watchTime;

    public UserVideoHistoryBatchDto(Long videoId, Long watchTime) {
        this.videoId = videoId;
        this.watchTime = watchTime;
    }
}
