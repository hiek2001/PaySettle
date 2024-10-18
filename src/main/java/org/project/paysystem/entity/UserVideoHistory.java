package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="userVideoHistory")
public class UserVideoHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VideoStatus status;

    private LocalDateTime pausedTime;

    private Long playTime;

    @Builder
    public UserVideoHistory(User user, Video video, VideoStatus status, Long playTime) {
        this.user = user;
        this.video = video;
        this.status = status;
        this.pausedTime = LocalDateTime.now();
        this.playTime = playTime;
    }

    @Builder
    public UserVideoHistory(VideoStatus status, Long playTime) {
        this.status = status;
        this.pausedTime = LocalDateTime.now();
        this.playTime = this.playTime + playTime;
    }

}
