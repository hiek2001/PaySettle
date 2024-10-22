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
@Table(name="video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    private Long duration; // 동영상 길이
    private Long videViews; // 조회수
    private String videoUrl; // 동영상 URL

    @Builder
    public Video(Long duration, String videoUrl, User user) {
        this.user = user;
        this.duration = duration;
        this.videViews = 0L;
        this.videoUrl = videoUrl;
    }

    public void updateVideoViews() {
        this.videViews++;
    }
}
