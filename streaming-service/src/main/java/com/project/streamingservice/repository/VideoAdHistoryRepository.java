package com.project.streamingservice.repository;

import com.project.streamingservice.entity.VideoAdHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoAdHistoryRepository extends JpaRepository<VideoAdHistory, Long> {
}
