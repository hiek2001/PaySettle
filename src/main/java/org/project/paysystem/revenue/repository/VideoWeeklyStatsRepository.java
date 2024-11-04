package org.project.paysystem.revenue.repository;

import org.project.paysystem.revenue.dto.RankVideoInfoDto;
import org.project.paysystem.revenue.entity.VideoWeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VideoWeeklyStatsRepository extends JpaRepository<VideoWeeklyStats, Integer> {

    @Query("SELECT vws.createdAt FROM VideoWeeklyStats vws ORDER BY vws.createdAt DESC LIMIT 1")
    LocalDate findWeeklyLastUpdate();

    @Query("SELECT new org.project.paysystem.dto.RankVideoInfoDto(vws.video, vws.weeklyViews, 0) FROM VideoWeeklyStats vws "+
            "WHERE vws.createdAt = :targetDate ORDER BY vws.weeklyViews DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByWeeklyViewsDesc(@Param("targetDate") LocalDate targetDate);

    @Query("SELECT new org.project.paysystem.dto.RankVideoInfoDto(vws.video, 0, vws.weeklyWatchTime) FROM VideoWeeklyStats vws "+
            "WHERE vws.createdAt = :targetDate ORDER BY vws.weeklyWatchTime DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByWeeklyWatchTimeDesc(@Param("targetDate") LocalDate nextSunday);
}
