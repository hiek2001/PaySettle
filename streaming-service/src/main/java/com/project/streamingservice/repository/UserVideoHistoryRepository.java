package com.project.streamingservice.repository;

import com.project.streamingservice.dto.UserVideoHistoryBatchDto;
import com.project.streamingservice.entity.UserVideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface UserVideoHistoryRepository extends JpaRepository<UserVideoHistory, Long> {
    UserVideoHistory findByVideoIdAndUserId(Long videoId, Long userId);

    // batch
    @Query("SELECT new com.project.streamingservice.dto.UserVideoHistoryBatchDto(u.video.id, SUM(u.watchTime)) " +
            "FROM UserVideoHistory u " +
            "GROUP BY u.video.id ")
    List<UserVideoHistoryBatchDto> findTodayWatchTime();

    @Query("SELECT u.video.id FROM UserVideoHistory u " +
            "WHERE u.id = (SELECT MAX(u2.id) FROM UserVideoHistory u2 WHERE u2.video.id = u.video.id)")
    List<Long> findLatestHistoryByIds();

    @Query("SELECT new com.project.streamingservice.dto.UserVideoHistoryBatchDto(u.video.id, SUM(u.watchTime)) " +
            "FROM UserVideoHistory u GROUP BY u.video.id"
    ) List<UserVideoHistoryBatchDto> findTotalWatchTimeByVideo();
}
