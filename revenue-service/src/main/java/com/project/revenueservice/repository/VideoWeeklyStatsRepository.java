package com.project.revenueservice.repository;

import com.project.revenueservice.dto.RankVideoInfoDto;
import com.project.revenueservice.entity.VideoWeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VideoWeeklyStatsRepository extends JpaRepository<VideoWeeklyStats, Integer> {

    @Query("SELECT vws.createdAt FROM VideoWeeklyStats vws ORDER BY vws.createdAt DESC LIMIT 1")
    LocalDate findWeeklyLastUpdate();

    @Query("SELECT new com.project.revenueservice.dto.RankVideoInfoDto(vws.videoId, vws.weeklyViews, 0) FROM VideoWeeklyStats vws "+
            "WHERE vws.createdAt = :targetDate ORDER BY vws.weeklyViews DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByWeeklyViewsDesc(@Param("targetDate") LocalDate targetDate);

    @Query("SELECT new com.project.revenueservice.dto.RankVideoInfoDto(vws.videoId, 0, vws.weeklyWatchTime) FROM VideoWeeklyStats vws "+
            "WHERE vws.createdAt = :targetDate ORDER BY vws.weeklyWatchTime DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByWeeklyWatchTimeDesc(@Param("targetDate") LocalDate nextSunday);
}
