package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name="videoCumulativeStats")
public class VideoCumulativeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    private long cumulativeViews; // 누적 조회수 , 집계 데이터로써 관리하기 위해 별도로 사용
    private long cumulativeWatchTime; // 누적 재생 시간

    private LocalDate createdAt;


    public VideoCumulativeStats(Video video, long cumulativeViews, long cumulativeWatchTime) {
        this.video = video;
        this.cumulativeViews = cumulativeViews;
        this.cumulativeWatchTime = cumulativeWatchTime;
        this.createdAt = LocalDate.now();
    }

    // Setter
    public void updateCumulativeWatchTime(long watchTime) {
        this.cumulativeWatchTime = watchTime;
        this.createdAt = LocalDate.now();
    }

    // Builder
    public static VideoCumulativeStatsBuilder builder() {
        return new VideoCumulativeStatsBuilder();
    }

    public static class VideoCumulativeStatsBuilder {
        private Video video;
        private long cumulativeViews;
        private long cumulativeWatchTime;

        public VideoCumulativeStatsBuilder video(Video video) {
            this.video = video;
            return this;
        }

        public VideoCumulativeStatsBuilder cumulativeWatchTime(long cumulativeWatchTime) {
            this.cumulativeWatchTime = cumulativeWatchTime;
            return this;
        }

        public VideoCumulativeStats build() {
            return new VideoCumulativeStats(video, cumulativeViews, cumulativeWatchTime);
        }
    }
}
