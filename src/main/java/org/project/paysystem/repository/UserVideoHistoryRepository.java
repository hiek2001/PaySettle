package org.project.paysystem.repository;

import org.project.paysystem.entity.UserVideoHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserVideoHistoryRepository extends JpaRepository<UserVideoHistory, Long> {
    UserVideoHistory findByVideoIdAndUserId(Long videoId, Long userId);

    // batch
    @Query("SELECT u FROM UserVideoHistory u " +
            "WHERE u.id = (SELECT MAX(u2.id) FROM UserVideoHistory u2 WHERE u2.video.id = u.video.id)")
    Page<UserVideoHistory> findLatestHistoryByVideo(Pageable pageable);
}
