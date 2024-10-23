package org.project.paysystem.service;

import jakarta.transaction.Transactional;
import org.project.paysystem.dto.VideoControlReqeustDto;
import org.project.paysystem.entity.UserVideoHistory;
import org.project.paysystem.entity.VideoStatus;
import org.springframework.stereotype.Service;

@Service
public class UserVideoHistoryService {

    @Transactional
    public void updateVideoStatus(UserVideoHistory userVideoHistory, VideoStatus videoStatus) {
        userVideoHistory.updateVideoStatus(videoStatus);
    }

    @Transactional
    public void updateVideoHistory(UserVideoHistory userVideoHistory, VideoStatus videoStatus, VideoControlReqeustDto requestDto) {
        userVideoHistory.updateVideoHistory(videoStatus, requestDto.getWatchTime(), requestDto.getPausedTime());

    }
}
