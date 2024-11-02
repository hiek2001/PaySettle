package com.project.revenueservice.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class VideoStatsJobFlow {
    private final JobRepository jobRepository;

    private final Job videoCumulativeJob;
    private final Job videoDailyStatsJob;
    private final Job videoDailyRevenueJob;
    private final Job adDailyRevenueJob;

    @Bean
    public Job combinedJob() {
        return new JobBuilder("combinedJob", jobRepository)
                .start(cumulativeJobFlow(videoCumulativeJob)).on("FAILED").end() // 실패하면 종료
                .from(cumulativeJobFlow(videoCumulativeJob)).on("*").to(dailyStatsJobFlow(videoDailyStatsJob)) // 이 외이면 일별 통계 시작
                .from(dailyStatsJobFlow(videoDailyStatsJob)).on("FAILED").end() // 일별 통계 실패하면 종료
                .from(dailyStatsJobFlow(videoDailyStatsJob)).on("*").to(revenueJobFlow(videoDailyRevenueJob))// 이 외이면 영상 일별 정산 시작
                .from(revenueJobFlow(videoDailyRevenueJob)).on("FAILED").end() // 영상 일별 정산 실패하면 종료
                .from(revenueJobFlow(videoDailyRevenueJob)).on("*").to(adRevenueJobFlow(adDailyRevenueJob)) // 이 외이면 광고 일별 정산 시작
                .end() // job 종료
                .build();
    }

    @Bean
    public Flow cumulativeJobFlow(Job videoCumulativeJob) {
        return new FlowBuilder<Flow>("videoCumulativeJobFlow")
                .start(new JobStepBuilder(new StepBuilder("videoCumulativeStep", jobRepository))
                        .job(videoCumulativeJob)
                        .build())
                .build();
    }

    @Bean
    public Flow dailyStatsJobFlow(Job videoDailyStatsJob) {
        return new FlowBuilder<Flow>("dailyStatsJobFlow")
                .start(new JobStepBuilder(new StepBuilder("videoDailyStatsStep", jobRepository))
                        .job(videoDailyStatsJob)
                        .build())
                .build();
    }

    @Bean
    public Flow revenueJobFlow(Job videoDailyRevenueJob) {
        return new FlowBuilder<Flow>("revenueJobFlow")
                .start(new JobStepBuilder(new StepBuilder("videoDailyRevenueStep", jobRepository))
                        .job(videoDailyRevenueJob)
                        .build())
                .build();
    }

    @Bean
    public Flow adRevenueJobFlow(Job adDailyRevenueJob) {
        return new FlowBuilder<Flow>("adRevenueJobFlow")
                .start(new JobStepBuilder(new StepBuilder("adDailyRevenueStep", jobRepository))
                        .job(adDailyRevenueJob)
                        .build())
                .build();
    }


}
