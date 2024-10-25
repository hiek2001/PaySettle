package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name="video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long duration; // 동영상 길이
    private Long videoViews; // 조회수
    private String videoUrl; // 동영상 URL

    public void updateVideoViews() {
        this.videoViews++;
    }
}
