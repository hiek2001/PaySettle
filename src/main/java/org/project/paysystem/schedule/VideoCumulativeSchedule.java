package org.project.paysystem.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j(topic="동영상 누적 조회수, 재생 시간 N일차 생성 스케줄러")
@Configuration
public class VideoCumulativeSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public VideoCumulativeSchedule(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    // 매일 저녁 23시 59분에 스케줄링 설정
    @Scheduled(cron = "59 23 * * * *", zone = "Asia/Seoul")
    public void VideoViewsCumulativeJob() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String date = sdf.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("videoCumulativeJob"), jobParameters);
    }

}
