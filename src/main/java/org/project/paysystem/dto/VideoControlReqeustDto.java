package org.project.paysystem.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VideoControlReqeustDto {
    private long pausedTime;
    private long watchTime;

    public void updatePausedTime(long pausedTime) {
        this.pausedTime = pausedTime;
    }
}
