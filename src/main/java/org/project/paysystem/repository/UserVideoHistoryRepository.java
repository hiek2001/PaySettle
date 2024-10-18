package org.project.paysystem.repository;

import org.project.paysystem.entity.UserVideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVideoHistoryRepository extends JpaRepository<UserVideoHistory, Long> {
    UserVideoHistory findByVideoIdAndUserId(Long videoId, Long userId);
}
