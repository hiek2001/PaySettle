package org.project.paysystem.streaming.service;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.streaming.dto.UserHistoryResponseDto;
import org.project.paysystem.streaming.entity.*;
import org.project.paysystem.user.dto.UserResponseDto;
import org.project.paysystem.streaming.dto.VideoControlReqeustDto;
import org.project.paysystem.streaming.exception.UserHistoryNotFoundException;
import org.project.paysystem.streaming.exception.VideoNotFoundException;
import org.project.paysystem.streaming.repository.AdRepository;
import org.project.paysystem.streaming.repository.UserVideoHistoryRepository;
import org.project.paysystem.streaming.repository.VideoAdHistoryRepository;
import org.project.paysystem.streaming.repository.VideoRepository;
import org.project.paysystem.user.entity.User;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final VideoRepository videoRepository;
    private final UserVideoHistoryRepository userHistoryRepository;
    private final AdRepository adRepository;
    private final VideoAdHistoryRepository videoAdHistoryRepository;

    private final MessageSource messageSource;

    @Transactional
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

        // 회원의 동영상 재생 내역 최초 저장
        if(userVideoHistory == null) {
            userVideoHistory = UserVideoHistory.builder()
                    .user(user)
                    .video(currentVideo)
                    .status(VideoStatus.PLAY)
                    .watchTime(0L)
                    .buildForFullData();

            userHistoryRepository.save(userVideoHistory);
        }

        // 재생 내역이 있기 때문에, watchTime은 그대로
        // 동영상 상태만 변경
        if(userVideoHistory.getStatus() == VideoStatus.END) {
            userVideoHistory.updateVideoStatus(VideoStatus.PLAY);
        }

        UserResponseDto userResponseDto = new UserResponseDto(userVideoHistory.getUser());

        return UserHistoryResponseDto.builder()
                .user(userResponseDto)
                .video(userVideoHistory.getVideo())
                .status(userVideoHistory.getStatus())
                .build();
    }

    @Transactional
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

        // 시청 완료
        if(currentVideo.getDuration() <= requestDto.getPausedTime()) {
            requestDto.updatePausedTime(0);
            userHistory.updateVideoHistory(VideoStatus.END, requestDto);
        } else {
            // 동영상 "재생" -> 버튼 클릭 시 "정지"로 전환
            if(userHistory.getStatus() == VideoStatus.PLAY) {
                userHistory.updateVideoHistory(VideoStatus.PAUSE, requestDto);
            } else {
                // 동영상 "정지" -> 버튼 클릭 시 "재생"으로 전환
                userHistory.updateVideoHistory(VideoStatus.PLAY, requestDto);
            }
        }

        UserResponseDto userResponseDto = new UserResponseDto(userHistory.getUser());
        return UserHistoryResponseDto.builder()
                .user(userResponseDto)
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
        Ad insertAd = adRepository.findRandomAdByHash();

        // 해당 동영상에 광고 연결하여 저장
        VideoAdHistory newVideoAdHistory = VideoAdHistory.builder()
                .video(video)
                .ad(insertAd)
                .build();

        videoAdHistoryRepository.save(newVideoAdHistory);
    }
}
