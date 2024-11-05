package com.project.revenueservice.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;

@Slf4j(topic="동영상 누적 조회수, 재생 시간 N일차, 일별 통계, 일별 정산 실행 스케줄러")
@Configuration
public class VideoCumulativeSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public VideoCumulativeSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    // 매일 저녁 23시 59분에 스케줄링 설정
    // @Scheduled(cron = "*/5 * * * * *", zone = "Asia/Seoul") // 테스트용
    @Scheduled(cron = "0 59 23 * * *", zone = "Asia/Seoul")
    public void combinedJob() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String date = sdf.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("currentDate", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("combinedJob"), jobParameters);

        // 일요일이면 주간 스케줄러를 추가 실행
        if (isSunday()) {
            runWeeklyJob(jobParameters);
        }
    }

    private void runWeeklyJob(JobParameters jobParameters) throws Exception {
        jobLauncher.run(jobRegistry.getJob("videoWeeklyStatsJob"), jobParameters);
    }

    private boolean isSunday() {
        return LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
