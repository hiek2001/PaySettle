package org.project.paysystem.streaming.dto;

import lombok.Builder;
import lombok.Getter;
import org.project.paysystem.streaming.entity.Video;
import org.project.paysystem.streaming.entity.VideoStatus;
import org.project.paysystem.user.dto.UserResponseDto;

@Getter
@Builder
public class UserHistoryResponseDto {
    private Long id;
    private UserResponseDto user;
    private Video video;
    private VideoStatus status;

}
