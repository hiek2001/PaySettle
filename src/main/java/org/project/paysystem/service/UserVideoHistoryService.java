package org.project.paysystem.service;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.UserHistoryResponseDto;
import org.project.paysystem.dto.VideoControlReqeustDto;
import org.project.paysystem.entity.User;
import org.project.paysystem.entity.UserVideoHistory;
import org.project.paysystem.entity.Video;
import org.project.paysystem.entity.VideoStatus;
import org.project.paysystem.exception.UserHistoryNotFoundException;
import org.project.paysystem.repository.UserVideoHistoryRepository;
import org.project.paysystem.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserVideoHistoryService {

    private final VideoRepository videoRepository;
    private final UserVideoHistoryRepository userHistoryRepository;

    private Video currentVideo;

    public UserHistoryResponseDto createHistory(Long videoId, User user) {
        currentVideo = videoRepository.findById(videoId).orElseThrow(() ->
                new NullPointerException("해당 동영상이 없습니다.")
        );

        // 동영상 조회수 증가
        currentVideo.updateVideoViews();

        UserVideoHistory userVideoHistory = userHistoryRepository.findByVideoIdAndUserId(videoId, user.getId());
        if(userVideoHistory == null || userVideoHistory.getStatus() == VideoStatus.END) {
            // 회원의 동영상 재생 내역 최초 저장
             userVideoHistory = UserVideoHistory.builder()
                    .user(user)
                    .video(currentVideo)
                    .status(VideoStatus.PLAY)
                    .playTime(0L)
                    .build();

            userHistoryRepository.save(userVideoHistory);
        }

        return UserHistoryResponseDto.builder()
                .user(userVideoHistory.getUser())
                .video(userVideoHistory.getVideo())
                .status(userVideoHistory.getStatus())
                .build();
    }

    public void updateVideoPlayback(Long videoId, VideoControlReqeustDto requestDto, User user) {
        UserVideoHistory userHistory = userHistoryRepository.findByVideoIdAndUserId(videoId, user.getId());
        if(userHistory == null) {
            throw new UserHistoryNotFoundException("회원이 재생한 내역이 없습니다.");
        }

        LocalDateTime currentPausedTime =  LocalDateTime.now();
        Duration duration = Duration.between(currentPausedTime, requestDto.getPausedTime());
        Long currentPlayTime = duration.toSeconds(); // 초 단위로 변환

        // 시청 완료
        if(currentVideo.getDuration() < currentPlayTime) {
            UserVideoHistory.builder()
                    .status(VideoStatus.END)
                    .playTime(currentVideo.getDuration())
                    .build();
        } else {
            // 동영상 "재생" -> 버튼 클릭 시 "정지"로 전환
            if(userHistory.getStatus() == VideoStatus.PLAY) {
                UserVideoHistory.builder()
                        .status(VideoStatus.PAUSE)
                        .playTime(currentPlayTime)
                        .build();
            } else { // 동영상 "정지" -> 버튼 클릭 시 "재생"으로 전환
                UserVideoHistory.builder()
                        .status(VideoStatus.PLAY)
                        .playTime(currentPlayTime)
                        .build();
            }
        }

        userHistoryRepository.save(userHistory);
    }
}
