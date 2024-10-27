package org.project.paysystem.repository;


import org.project.paysystem.entity.VideoDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VideoDailyStatsRepository extends JpaRepository<VideoDailyStats, Long> {
    // batch
    VideoDailyStats findByVideoId(Long videoId);

}
