package org.project.paysystem.revenue.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.project.paysystem.streaming.entity.Video;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="videoDailyStats")
public class VideoDailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    private long dailyViews;
    private long dailyWatchTime;

    private LocalDate createdAt;

    public VideoDailyStats(Video video, long dailyViews, long dailyWatchTime) {
        this.video = video;
        this.dailyViews = dailyViews;
        this.dailyWatchTime = dailyWatchTime;
        this.createdAt = LocalDate.now();
    }

    // Setter
    public void updateDailyWatchTime(long watchTime) {
        this.dailyWatchTime = watchTime;
        this.createdAt = LocalDate.now();
    }

    // Builder
    public static VideoDailyStats.VideoDailyStatsBuilder builder() {
        return new VideoDailyStats.VideoDailyStatsBuilder();
    }

    public static class VideoDailyStatsBuilder {
        private Video video;
        private long dailyViews;
        private long dailyWatchTime;

        public VideoDailyStatsBuilder video(Video video) {
            this.video = video;
            return this;
        }
        public VideoDailyStatsBuilder dailyViews(long dailyViews) {
            this.dailyViews = dailyViews;
            return this;
        }
        public VideoDailyStatsBuilder dailyWatchTime(long dailyWatchTime) {
            this.dailyWatchTime = dailyWatchTime;
            return this;
        }
        public VideoDailyStats build() {
            return new VideoDailyStats(video, dailyViews, dailyWatchTime);
        }

    }
}
