package com.project.streamingservice.service;

import com.project.streamingservice.dto.*;
import com.project.streamingservice.entity.*;
import com.project.streamingservice.exception.UserHistoryNotFoundException;
import com.project.streamingservice.exception.VideoNotFoundException;
import com.project.streamingservice.repository.AdRepository;
import com.project.streamingservice.repository.UserVideoHistoryRepository;
import com.project.streamingservice.repository.VideoAdHistoryRepository;
import com.project.streamingservice.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final VideoRepository videoRepository;
    private final UserVideoHistoryRepository userHistoryRepository;
    private final AdRepository adRepository;
    private final VideoAdHistoryRepository videoAdHistoryRepository;

    private final MessageSource messageSource;

    @Transactional
    public UserHistoryResponseDto createUserVideoHistory(Long videoId, UserRequestDto userRequestDto) {

        log.info("userId {}",userRequestDto.getId());

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

        UserVideoHistory userVideoHistory = userHistoryRepository.findByVideoIdAndUserId(videoId, userRequestDto.getId());

        // 회원의 동영상 재생 내역 최초 저장
        if(userVideoHistory == null) {
            userVideoHistory = UserVideoHistory.builder()
                    .user(userRequestDto.getId())
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

        return UserHistoryResponseDto.builder()
                .userId(userRequestDto.getId())
                .video(userVideoHistory.getVideo())
                .status(userVideoHistory.getStatus())
                .build();
    }

    @Transactional
    public UserHistoryResponseDto updateVideoPlayback(Long videoId, VideoControlReqeustDto requestDto) {
        Video currentVideo = videoRepository.findById(videoId).orElseThrow(() ->
                new VideoNotFoundException(messageSource.getMessage(
                        "not.found.video",
                        null,
                        "Not Found Video "+videoId,
                        Locale.getDefault()
                ))
        );

        UserVideoHistory userHistory = userHistoryRepository.findByVideoIdAndUserId(videoId, requestDto.getUser().getId());
        if(userHistory == null) {
            throw new UserHistoryNotFoundException(messageSource.getMessage(
                    "not.found.userHistory",
                    null,
                    "Not Found User History "+videoId+" , "+requestDto.getUser().getId(),
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

        return UserHistoryResponseDto.builder()
                .userId(requestDto.getUser().getId())
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

    public List<VideoDto> getAllVideos() {
        return videoRepository.batchFindAll();
    }

    public List<VideoDto> getVideoAfterId(Long afterId, int pageSize) {
        return videoRepository.batchFindAfterId(afterId, pageSize);
    }

    public List<Long> getAllVideoIds() {
        return videoRepository.findAllIds();
    }

    public List<UserVideoHistoryBatchDto> getVideoByDay() {
        return userHistoryRepository.findTodayWatchTime();
    }

    public List<Long> getLatestVideos() {
        return userHistoryRepository.findLatestHistoryByIds();
    }

    public List<UserVideoHistoryBatchDto> getTotalWatchTimeByVideo() {
        return userHistoryRepository.findTotalWatchTimeByVideo();
    }

    public List<AdCountBatchDto> getAdCountByDate(@RequestParam LocalDate currentDate) {
        return videoAdHistoryRepository.getAdCountByDate(currentDate);
    }
}
