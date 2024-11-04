package com.project.streamingservice.entity;

import com.project.streamingservice.dto.VideoControlReqeustDto;
import com.project.userservice.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name="userVideoHistory")
public class UserVideoHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="user_id")
//    private User user;
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="video_id")
    private Video video;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VideoStatus status;

    private long pausedTime;

    private long watchTime;

    // setter
    public void updateVideoStatus(VideoStatus status) {
        this.status = status;
    }

    public void updateVideoHistory(VideoStatus videoStatus, VideoControlReqeustDto reqeustDto) {
        this.status = videoStatus;
        this.watchTime = this.watchTime + reqeustDto.getWatchTime();
        this.pausedTime = reqeustDto.getPausedTime();
    }

    // builder
    public static UserVideoHistoryBuilder builder() {
        return new UserVideoHistoryBuilder();
    }

    public static class UserVideoHistoryBuilder {
        private Long userId;
        private Video video;
        private VideoStatus status;
        private long pausedTime;
        private long watchTime;

        public UserVideoHistoryBuilder user(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserVideoHistoryBuilder video(Video video) {
            this.video = video;
            return this;
        }
        public UserVideoHistoryBuilder status(VideoStatus status) {
            this.status = status;
            return this;
        }
        public UserVideoHistoryBuilder pausedTime(long pausedTime) {
            this.pausedTime = pausedTime;
            return this;
        }
        public UserVideoHistoryBuilder watchTime(long watchTime) {
            this.watchTime = watchTime;
            return this;
        }
        public UserVideoHistory buildForFullData() {
            return new UserVideoHistory(userId, video, status, pausedTime, watchTime);
        }
    }


    private UserVideoHistory(Long userId, Video video, VideoStatus status, long watchTime, long pausedTime) {
        this.userId = userId;
        this.video = video;
        this.status = status;
        this.pausedTime = pausedTime;
        this.watchTime = watchTime;
    }

}
