package org.project.paysystem.streaming.dto;

import lombok.Getter;

@Getter
public class VideoControlReqeustDto {
    private long pausedTime;
    private long watchTime;

    public void updatePausedTime(long pausedTime) {
        this.pausedTime = pausedTime;
    }
}
