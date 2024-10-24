package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="videoCumulativeStats")
public class VideoCumulativeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    private long cumulativeViews; // 누적 조회수
    private long cumulativeWatchTime; // 누적 재생 시간

    private LocalDate createdAt;
}
