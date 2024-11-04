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
@Table(name="adDailyRevenue")
public class AdDailyRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video; // 동영상에 삽입된 광고로 정산

    private Long adAmount; // 광고 정산 금액

    private LocalDate createdAt;

    @Builder
    public AdDailyRevenue(Video video, Long adAmount, LocalDate createdAt) {
        this.video = video;
        this.adAmount = adAmount;
        this.createdAt = LocalDate.now();
    }
}
