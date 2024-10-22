package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="VideoAdHistory")
public class VideoAdHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ads_id")
    private Ads ads;

    private Long watchedTime; // 광고를 시청한 시간

    @Builder
    public VideoAdHistory(Video video, Ads ads) {
        this.video = video;
        this.ads = ads;
        this.watchedTime = System.currentTimeMillis();
    }
}
