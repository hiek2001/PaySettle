package org.project.paysystem.repository;

import org.project.paysystem.entity.VideoCumulativeStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoCumulativeStatsRepository extends JpaRepository<VideoCumulativeStats, Long> {
    Optional<VideoCumulativeStats> findByVideoId(Long videoId); // batch
}
