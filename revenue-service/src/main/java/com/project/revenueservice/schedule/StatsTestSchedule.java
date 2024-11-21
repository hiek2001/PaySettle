package com.project.revenueservice.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j(topic="동영상 주간 통계 스케줄러")
@Configuration
public class StatsTestSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public StatsTestSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    // 매주 일요일
    @Scheduled(cron = "0 59 23 7 * *", zone = "Asia/Seoul")
    public void videoDailyStatsJob() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String date = sdf.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("currentDate", date)
                .toJobParameters();

        //jobLauncher.run(jobRegistry.getJob("videoWeeklyStatsJob"), jobParameters);
        jobLauncher.run(jobRegistry.getJob("videoDailyStatsJob"), jobParameters);
    }
}
