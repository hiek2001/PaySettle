package com.project.revenueservice.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VideoBatchInsertService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final LocalDate date = LocalDate.parse("2024-11-05");

    private static final int BATCH_SIZE = 50000;

    public void insertDummyData(int startRecord, int endRecord) {
        String sql = "INSERT INTO video (duration, id, video_views, video_url) VALUES (?, ?, ?, ?)";

        List<Video> videoList = new ArrayList<>();

        for (int i = startRecord; i <= startRecord + endRecord; i++) {
            Video video = new Video(
                    100 + i % 100, //100~199 범위로 설정
                    i,
                    100 + i % 100,
                    "video.url/"+i
            );
            videoList.add(video);

            // BATCH_SIZE마다 배치 삽입
            if(i % BATCH_SIZE == 0) {
                batchInsert(videoList, sql);
                videoList.clear();
                log.info("======= insertDummyData i번째 : "+ i);
            }
        }
        // 남은 데이터 삽입
        if (!videoList.isEmpty()) {
            batchInsert(videoList, sql);
        }

    }

    private void batchInsert(List<Video> videoList, String sql) {
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Video video = videoList.get(i);
                ps.setInt(1, video.getDuration());
                ps.setInt(2, video.getId());
                ps.setInt(3, video.getVideoViews());
                ps.setString(4, video.getVideoUrl());
            }

            @Override
            public int getBatchSize() {
                return videoList.size();
            }
        });
    }

    public void insertDummyCumulateData(int startRecord, int endRecord) {
        String sql = "INSERT INTO video_cumulative_stats (id, video_id, cumulative_views, cumulative_watch_time, created_at) VALUES (?,?,?,?,?)";

        List<VideoCumulativeStats> list = new ArrayList<>();

        for(int i = startRecord ; i <= startRecord + endRecord ; i++) {
            VideoCumulativeStats videoCumulativeStats = new VideoCumulativeStats(
                    i,
                    i, // 50~150 범위 설정
                    50 + i % 101,
                    0,
                    date

            );
            list.add(videoCumulativeStats);

            if(i % BATCH_SIZE == 0) {
                batchInsertCumulative(list, sql);
                list.clear();
                log.info("========= insertDummyCumulateData i번째 : {}",i);
            }
        }

        if (!list.isEmpty()) {
            batchInsertCumulative(list, sql);
        }
    }

    private void batchInsertCumulative(List<VideoCumulativeStats> list, String sql) {
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                VideoCumulativeStats vcs = list.get(i);
                ps.setInt(1, vcs.getId());
                ps.setInt(2, vcs.getVideoId());
                ps.setInt(3, vcs.getCumulativeViews());
                ps.setInt(4, vcs.getCumulativeWatchTime());
                ps.setObject(5, vcs.getCreatedAt());
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
    }

    // VideoCumulativeStats 클래스 (더미 데이터용)
    public static class  VideoCumulativeStats {
        private int id;
        private int videoId;
        private int cumulativeViews;
        private int cumulativeWatchTime;
        private LocalDate createdAt;

        public VideoCumulativeStats(int id, int videoId, int cumulativeViews, int cumulativeWatchTime, LocalDate createdAt) {
            this.id = id;
            this.videoId = videoId;
            this.cumulativeViews = cumulativeViews;
            this.cumulativeWatchTime = cumulativeWatchTime;
            this.createdAt = createdAt;
        }

        public int getId() {return id;}
        public int getVideoId() {return videoId;}
        public int getCumulativeViews() {return cumulativeViews;}
        public int getCumulativeWatchTime() {return cumulativeWatchTime;}
        public LocalDate getCreatedAt() {return createdAt;}

    }
    // Video 클래스 (더미 데이터용)
    public static class Video {
        private int duration;
        private int id;
        private int videoViews;
        private String videoUrl;

        public Video(int duration, int id, int videoViews, String videoUrl) {
            this.duration = duration;
            this.id = id;
            this.videoViews = videoViews;
            this.videoUrl = videoUrl;
        }

        public int getDuration() { return duration; }
        public int getId() { return id; }
        public int getVideoViews() { return videoViews; }
        public String getVideoUrl() { return videoUrl; }
    }
}
