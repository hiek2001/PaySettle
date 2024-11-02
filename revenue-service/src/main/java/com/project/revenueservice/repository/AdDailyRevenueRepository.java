package com.project.revenueservice.repository;


import com.project.revenueservice.dto.RevenueInfoDto;
import com.project.revenueservice.entity.AdDailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AdDailyRevenueRepository extends JpaRepository<AdDailyRevenue, Long> {

    @Query("SELECT adr.createdAt FROM AdDailyRevenue adr ORDER BY adr.createdAt DESC LIMIT 1")
    LocalDate findLastUpdate();

    @Query("SELECT new com.project.revenueservice.dto.RevenueInfoDto(adr.videoId, adr.adAmount) FROM AdDailyRevenue adr " +
            "WHERE adr.createdAt = :targetDate"
    ) List<RevenueInfoDto> findRevenueByDay(LocalDate targetDate);

    @Query("SELECT new com.project.revenueservice.dto.RevenueInfoDto(adr.videoId, SUM(adr.adAmount)) FROM AdDailyRevenue adr " +
            "WHERE adr.createdAt BETWEEN :startDate AND :endDate "+
            "GROUP BY adr.videoId"
    ) List<RevenueInfoDto> findRevenueByWeek(LocalDate startDate, LocalDate endDate);

    @Query("SELECT new com.project.revenueservice.dto.RevenueInfoDto(adr.videoId, SUM(adr.adAmount)) "+
            "FROM AdDailyRevenue adr "+
            "WHERE DATE_FORMAT(adr.createdAt, '%Y-%m') = :targetMonth " +
            "GROUP BY adr.videoId"
    ) List<RevenueInfoDto> findRevenueByMonth(String targetMonth);
}
