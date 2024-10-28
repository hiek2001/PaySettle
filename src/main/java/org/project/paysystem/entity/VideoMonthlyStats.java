package org.project.paysystem.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    private long monthlyViews;
    private long monthlyWatchTime;

    private LocalDate createdAt;

    @Builder
    public VideoMonthlyStats(Video video, long monthlyViews, long monthlyWatchTime, LocalDate createdAt) {
        this.video = video;
        this.monthlyViews = monthlyViews;
        this.monthlyWatchTime = monthlyWatchTime;
        this.createdAt = createdAt;
    }
}
