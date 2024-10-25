package org.project.paysystem.dto;

import lombok.Getter;

@Getter
public class UserVideoHistoryBatchDto {
    private Long videoId;
    private Long watchTime;

    public UserVideoHistoryBatchDto(Long videoId, Long watchTime) {
        this.videoId = videoId;
        this.watchTime = watchTime;
    }
}
