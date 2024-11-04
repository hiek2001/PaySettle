package com.project.revenueservice.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j(topic="광고 일별 정산 실행 스케줄러")
@RequiredArgsConstructor
@Configuration
public class AdDailyRevenueSchedule {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

   // @Scheduled(cron = "*/5 * * * * *", zone = "Asia/Seoul")
    public void adDailyRevenueJob() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String date = sdf.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("currentDate", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("adDailyRevenueJob"), jobParameters);
    }
}
