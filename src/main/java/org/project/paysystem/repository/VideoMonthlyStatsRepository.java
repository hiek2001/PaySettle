package org.project.paysystem.repository;

import org.project.paysystem.dto.RankVideoInfoDto;
import org.project.paysystem.entity.VideoMonthlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface VideoMonthlyStatsRepository extends JpaRepository<VideoMonthlyStats, Long> {

    @Query("SELECT vms.createdAt FROM VideoMonthlyStats vms ORDER BY vms.createdAt DESC LIMIT 1")
    LocalDate findMonthlyLastUpdate();

    @Query("SELECT new org.project.paysystem.dto.RankVideoInfoDto(vms.video, vms.monthlyViews) "+
            "FROM VideoMonthlyStats vms "+
            "WHERE DATE_FORMAT(vms.createdAt, '%Y-%m') = :targetMonth " +
            "ORDER BY vms.monthlyViews DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByMonthlyViewsDesc(@Param("targetMonth") YearMonth yearMonth);
}
