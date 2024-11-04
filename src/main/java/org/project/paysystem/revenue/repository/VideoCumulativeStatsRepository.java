package org.project.paysystem.revenue.repository;

import org.project.paysystem.revenue.entity.VideoCumulativeStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VideoCumulativeStatsRepository extends JpaRepository<VideoCumulativeStats, Long> {
    // batch
    @Query("SELECT vcs FROM VideoCumulativeStats vcs WHERE vcs.createdAt = :parsedDate AND vcs.video.id IN :videoList")
    Page<VideoCumulativeStats> findByCreatedAtAndVideoIn(LocalDate parsedDate, List<Long> videoList, Pageable pageable);

    Optional<VideoCumulativeStats> findByVideoIdAndCreatedAt(Long id, LocalDate parsedDate);
}
