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
}
