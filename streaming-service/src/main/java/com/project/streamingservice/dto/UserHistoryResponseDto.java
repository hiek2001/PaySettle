package com.project.streamingservice.dto;

import com.project.streamingservice.entity.Video;
import com.project.streamingservice.entity.VideoStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserHistoryResponseDto {
    private Long userId;
    private Video video;
    private VideoStatus status;

}
