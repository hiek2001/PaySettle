package org.project.paysystem.repository;

import org.project.paysystem.entity.VideoDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoDailyStatsRepository extends JpaRepository<VideoDailyStats, Long> {
    // batch
    VideoDailyStats findByVideoId(Long videoId);
}
