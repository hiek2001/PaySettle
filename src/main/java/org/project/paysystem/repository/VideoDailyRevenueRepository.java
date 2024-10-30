package org.project.paysystem.repository;

import org.project.paysystem.dto.RevenueInfoDto;
import org.project.paysystem.entity.VideoDailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VideoDailyRevenueRepository extends JpaRepository<VideoDailyRevenue, Long> {

    @Query("SELECT vdr.createdAt FROM VideoDailyRevenue vdr ORDER BY vdr.createdAt DESC LIMIT 1")
    LocalDate findLastUpdate();

    @Query("SELECT new org.project.paysystem.dto.RevenueInfoDto(vdr.video.id, vdr.videoAmount) FROM VideoDailyRevenue vdr " +
            "WHERE vdr.createdAt = :targetDate"
    ) List<RevenueInfoDto> findRevenueByDay(@Param("targetDate")LocalDate targetDate);

    @Query("SELECT new org.project.paysystem.dto.RevenueInfoDto(vdr.video.id, SUM(vdr.videoAmount)) FROM VideoDailyRevenue vdr " +
            "WHERE vdr.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY vdr.video.id"
    ) List<RevenueInfoDto> findRevenueByWeek(@Param("startDate")LocalDate startDate, @Param("endDate")LocalDate endDate);

    @Query("SELECT new org.project.paysystem.dto.RevenueInfoDto(vdr.video.id, SUM(vdr.videoAmount)) "+
            "FROM VideoDailyRevenue vdr "+
            "WHERE DATE_FORMAT(vdr.createdAt, '%Y-%m') = :targetMonth " +
            "GROUP BY vdr.video.id"
    )List<RevenueInfoDto> findRevenueByMonth(@Param("targetMonth")String targetMonth);
}
