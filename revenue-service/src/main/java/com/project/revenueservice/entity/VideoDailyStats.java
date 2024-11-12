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
@Table(name="videoDailyStats")
public class VideoDailyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId;

    private long dailyViews;
    private long dailyWatchTime;

    private LocalDate createdAt;

    @Builder
    public VideoDailyStats(Long videoId, long dailyViews, long dailyWatchTime) {
        this.videoId = videoId;
        this.dailyViews = dailyViews;
        this.dailyWatchTime = dailyWatchTime;
        this.createdAt = LocalDate.now();
    }

    // Setter
    public void updateDailyWatchTime(long watchTime) {
        this.dailyWatchTime = watchTime;
        this.createdAt = LocalDate.now();
    }

//    // Builder
//    public static VideoDailyStatsBuilder builder() {
//        return new VideoDailyStatsBuilder();
//    }
//
//    public static class VideoDailyStatsBuilder {
//        private Long videoId;
//        private long dailyViews;
//        private long dailyWatchTime;
//
//        public VideoDailyStatsBuilder video(Long videoId) {
//            this.videoId = videoId;
//            return this;
//        }
//        public VideoDailyStatsBuilder dailyViews(long dailyViews) {
//            this.dailyViews = dailyViews;
//            return this;
//        }
//        public VideoDailyStatsBuilder dailyWatchTime(long dailyWatchTime) {
//            this.dailyWatchTime = dailyWatchTime;
//            return this;
//        }
//        public VideoDailyStats build() {
//            return new VideoDailyStats(videoId, dailyViews, dailyWatchTime);
//        }
//
//    }
}
