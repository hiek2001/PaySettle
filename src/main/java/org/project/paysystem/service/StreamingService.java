package org.project.paysystem.service;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.UserHistoryResponseDto;
import org.project.paysystem.dto.VideoControlReqeustDto;
import org.project.paysystem.entity.*;
import org.project.paysystem.exception.UserHistoryNotFoundException;
import org.project.paysystem.exception.VideoNotFoundException;
import org.project.paysystem.repository.AdsRepository;
import org.project.paysystem.repository.UserVideoHistoryRepository;
import org.project.paysystem.repository.VideoAdHistoryRepository;
import org.project.paysystem.repository.VideoRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final VideoRepository videoRepository;
    private final UserVideoHistoryRepository userHistoryRepository;
    private final AdsRepository adsRepository;
    private final VideoAdHistoryRepository videoAdHistoryRepository;

    private final MessageSource messageSource;

    public UserHistoryResponseDto createUserVideoHistory(Long videoId, User user) {
        Video currentVideo = videoRepository.findById(videoId).orElseThrow(() ->
                new VideoNotFoundException(messageSource.getMessage(
                        "not.found.video",
                        null,
                        "Not Found Video "+videoId,
                        Locale.getDefault()
                ))
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
        }

        userHistoryRepository.save(userVideoHistory);

        return UserHistoryResponseDto.builder()
                .user(userVideoHistory.getUser())
                .video(userVideoHistory.getVideo())
                .status(userVideoHistory.getStatus())
                .build();
    }

    public UserHistoryResponseDto updateVideoPlayback(Long videoId, VideoControlReqeustDto requestDto, User user) {
        Video currentVideo = videoRepository.findById(videoId).orElseThrow(() ->
                new VideoNotFoundException(messageSource.getMessage(
                        "not.found.video",
                        null,
                        "Not Found Video "+videoId,
                        Locale.getDefault()
                ))
        );

        UserVideoHistory userHistory = userHistoryRepository.findByVideoIdAndUserId(videoId, user.getId());
        if(userHistory == null) {
            throw new UserHistoryNotFoundException(messageSource.getMessage(
                    "not.found.userHistory",
                    null,
                    "Not Found User History "+videoId+" , "+user.getId(),
                    Locale.getDefault()
            ));
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

        return UserHistoryResponseDto.builder()
                .user(userHistory.getUser())
                .video(userHistory.getVideo())
                .status(userHistory.getStatus())
                .build();
    }

    public void createVideoAdHistory(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() ->
                new VideoNotFoundException(messageSource.getMessage(
                        "not.found.video",
                        null,
                        "Not Found Video "+videoId,
                        Locale.getDefault()
                ))
        );

        // 동영상에 삽입할 광고 1개 랜덤으로 가져오기(실시간성 보장)
        Ads insertAd = adsRepository.findRandomAdByHash();

        // 해당 동영상에 광고 연결하여 저장
        VideoAdHistory newVideoAdHistory = VideoAdHistory.builder()
                .video(video)
                .ads(insertAd)
                .build();

        videoAdHistoryRepository.save(newVideoAdHistory);
    }
}
