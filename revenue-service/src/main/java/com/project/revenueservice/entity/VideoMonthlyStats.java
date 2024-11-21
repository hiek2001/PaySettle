package com.project.revenueservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="videoMonthlyStats")
public class VideoMonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;

    private long monthlyViews;
    private long monthlyWatchTime;

    private LocalDate createdAt;

    @Builder
    public VideoMonthlyStats(Long videoId, long monthlyViews, long monthlyWatchTime, LocalDate createdAt) {
        this.videoId = videoId;
        this.monthlyViews = monthlyViews;
        this.monthlyWatchTime = monthlyWatchTime;
        this.createdAt = createdAt;
    }
}
