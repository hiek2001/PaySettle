package com.project.revenueservice.repository;

import com.project.revenueservice.dto.RankVideoInfoDto;
import com.project.revenueservice.entity.VideoMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VideoMonthlyStatsRepository extends JpaRepository<VideoMonthlyStats, Long> {

    @Query("SELECT vms.createdAt FROM VideoMonthlyStats vms ORDER BY vms.createdAt DESC LIMIT 1")
    LocalDate findMonthlyLastUpdate();

    @Query("SELECT new com.project.revenueservice.dto.RankVideoInfoDto(vms.videoId, vms.monthlyViews, 0) "+
            "FROM VideoMonthlyStats vms "+
            "WHERE DATE_FORMAT(vms.createdAt, '%Y-%m') = :targetMonth " +
            "ORDER BY vms.monthlyViews DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByMonthlyViewsDesc(@Param("targetMonth") String yearMonth);

    @Query("SELECT new com.project.revenueservice.dto.RankVideoInfoDto(vms.videoId, 0, vms.monthlyWatchTime) "+
            "FROM VideoMonthlyStats vms "+
            "WHERE DATE_FORMAT(vms.createdAt, '%Y-%m') = :targetMonth " +
            "ORDER BY vms.monthlyWatchTime DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByMonthlyWatchTimeDesc(@Param("targetMonth") String targetMonth);
}
