package com.project.streamingservice.repository;

import com.project.streamingservice.dto.AdCountBatchDto;
import com.project.streamingservice.entity.VideoAdHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VideoAdHistoryRepository extends JpaRepository<VideoAdHistory, Long> {
    @Query("SELECT new com.project.streamingservice.dto.AdCountBatchDto(vah.video.id, COUNT(vah.ad.id)) " +
            "FROM VideoAdHistory vah " +
            "WHERE vah.createdAt = :currentDate " +
            "GROUP BY vah.video.id")
    List<AdCountBatchDto> getAdCountByDate(@Param("currentDate") LocalDate currentDate);
}
