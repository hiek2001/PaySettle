package org.project.paysystem.repository;


import org.project.paysystem.dto.RankVideoInfoDto;
import org.project.paysystem.entity.VideoDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface VideoDailyStatsRepository extends JpaRepository<VideoDailyStats, Long> {

    @Query("SELECT vds.createdAt FROM VideoDailyStats vds ORDER BY vds.createdAt DESC LIMIT 1")
    LocalDate findLastUpdate();

    @Query("SELECT new org.project.paysystem.dto.RankVideoInfoDto(vds.video, vds.dailyViews) "+
            "FROM VideoDailyStats vds "+
            "WHERE vds.createdAt = :targetDate "+
            "ORDER BY vds.dailyViews DESC LIMIT 5"
    ) List<RankVideoInfoDto> findTop5ByCreatedAtOrderByDailyViewsDesc(@Param("targetDate") LocalDate parsedDate);


    // batch
    VideoDailyStats findByVideoId(Long videoId);


}
