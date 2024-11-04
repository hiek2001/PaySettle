package org.project.paysystem.streaming.entity;

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
@Table(name="videoAdHistory")
public class VideoAdHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ad_id")
    private Ad ad;

    private LocalDate createdAt; // 광고를 시청한 시간

    @Builder
    public VideoAdHistory(Video video, Ad ad) {
        this.video = video;
        this.ad = ad;
        this.createdAt = LocalDate.now();
    }
}
