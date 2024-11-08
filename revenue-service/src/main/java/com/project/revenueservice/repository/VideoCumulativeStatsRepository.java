package com.project.revenueservice.repository;

import com.project.revenueservice.entity.VideoCumulativeStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VideoCumulativeStatsRepository extends JpaRepository<VideoCumulativeStats, Long> {
    // batch
    Optional<VideoCumulativeStats> findByVideoIdAndCreatedAt(Long id, LocalDate parsedDate);
}
