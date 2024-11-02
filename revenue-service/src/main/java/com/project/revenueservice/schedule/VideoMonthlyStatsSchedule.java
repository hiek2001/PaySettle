package com.project.revenueservice.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;

import java.time.YearMonth;

@Slf4j(topic="동영상 월별 통계 스케줄러")
@Configuration
public class VideoMonthlyStatsSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public VideoMonthlyStatsSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }


   // 매달 1일에 실행
    //@Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Seoul")
    public void videoMonthlyStatsJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetMonth", YearMonth.now().minusMonths(1).toString()) // 이전 달을 집계 대상으로 지정
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("videoMonthlyStatsJob"), jobParameters);
    }

}
