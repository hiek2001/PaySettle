package org.project.paysystem.revenue.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.project.paysystem.streaming.entity.Video;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    private long weeklyViews;
    private long weeklyWatchTime;

    private LocalDate createdAt;

    @Builder
    public VideoWeeklyStats(Video video, long weeklyViews, long weeklyWatchTime) {
        this.video = video;
        this.weeklyViews = weeklyViews;
        this.weeklyWatchTime = weeklyWatchTime;
        this.createdAt = LocalDate.now();
    }
}
