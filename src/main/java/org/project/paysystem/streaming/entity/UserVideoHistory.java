package org.project.paysystem.streaming.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.project.paysystem.streaming.dto.VideoControlReqeustDto;
import org.project.paysystem.user.entity.User;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

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
        private User user;
        private Video video;
        private VideoStatus status;
        private long pausedTime;
        private long watchTime;

        public UserVideoHistoryBuilder user(User user) {
            this.user = user;
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
            return new UserVideoHistory(user, video, status, pausedTime, watchTime);
        }
    }


    private UserVideoHistory(User user, Video video, VideoStatus status, long watchTime, long pausedTime) {
        this.user = user;
        this.video = video;
        this.status = status;
        this.pausedTime = pausedTime;
        this.watchTime = watchTime;
    }

}
