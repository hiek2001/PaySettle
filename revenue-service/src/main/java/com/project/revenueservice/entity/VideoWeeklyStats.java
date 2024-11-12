package com.project.revenueservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.project.streamingservice.entity.Video;

import java.time.LocalDate;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="videoWeeklyStats")
public class VideoWeeklyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;

    private long weeklyViews;
    private long weeklyWatchTime;

    private LocalDate createdAt;

    @Builder
    public VideoWeeklyStats(Long videoId, long weeklyViews, long weeklyWatchTime) {
        this.videoId = videoId;
        this.weeklyViews = weeklyViews;
        this.weeklyWatchTime = weeklyWatchTime;
        this.createdAt = LocalDate.now();
    }
}
