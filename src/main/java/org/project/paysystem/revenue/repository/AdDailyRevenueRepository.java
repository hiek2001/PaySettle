package org.project.paysystem.revenue.repository;

import org.project.paysystem.revenue.dto.RevenueInfoDto;
import org.project.paysystem.revenue.entity.AdDailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AdDailyRevenueRepository extends JpaRepository<AdDailyRevenue, Long> {

    @Query("SELECT adr.createdAt FROM AdDailyRevenue adr ORDER BY adr.createdAt DESC LIMIT 1")
    LocalDate findLastUpdate();

    @Query("SELECT new org.project.paysystem.dto.RevenueInfoDto(adr.video.id, adr.adAmount) FROM AdDailyRevenue adr " +
            "WHERE adr.createdAt = :targetDate"
    ) List<RevenueInfoDto> findRevenueByDay(LocalDate targetDate);

    @Query("SELECT new org.project.paysystem.dto.RevenueInfoDto(adr.video.id, SUM(adr.adAmount)) FROM AdDailyRevenue adr " +
            "WHERE adr.createdAt BETWEEN :startDate AND :endDate "+
            "GROUP BY adr.video.id"
    ) List<RevenueInfoDto> findRevenueByWeek(LocalDate startDate, LocalDate endDate);

    @Query("SELECT new org.project.paysystem.dto.RevenueInfoDto(adr.video.id, SUM(adr.adAmount)) "+
            "FROM AdDailyRevenue adr "+
            "WHERE DATE_FORMAT(adr.createdAt, '%Y-%m') = :targetMonth " +
            "GROUP BY adr.video.id"
    ) List<RevenueInfoDto> findRevenueByMonth(String targetMonth);
}
