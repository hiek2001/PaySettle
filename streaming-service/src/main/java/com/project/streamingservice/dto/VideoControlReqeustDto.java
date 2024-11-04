package com.project.streamingservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoControlReqeustDto {
    private long pausedTime;
    private long watchTime;
    private UserRequestDto user;

    @Builder
    public VideoControlReqeustDto(long pausedTime, long watchTime, UserRequestDto user) {
        this.pausedTime = pausedTime;
        this.watchTime = watchTime;
        this.user = user;
    }

    public void updatePausedTime(long pausedTime) {
        this.pausedTime = pausedTime;
    }
}
