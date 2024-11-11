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

    // Flow에 맞는 배치 잡 순차적으로 진행
    // 누적 N일차 -> 일별 통계 (-> 매주 일: 주별 통계 -> 매달 1일: 월별 통계)-> 영상 일별 정산 -> 광고 일별 정산
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

    // 누적 N일차 job flow
    @Bean
    public Flow cumulativeJobFlow(Job videoCumulativeJob) {
        return new FlowBuilder<Flow>(BatchConstants.VIDEO_CUMULATIVE+"JobFlow")
                .start(new JobStepBuilder(new StepBuilder(BatchConstants.VIDEO_CUMULATIVE+"Step", jobRepository))
                        .job(videoCumulativeJob)
                        .build())
                .build();
    }

    // 일별 통계 job flow
    @Bean
    public Flow dailyStatsJobFlow(Job videoDailyStatsJob) {
        return new FlowBuilder<Flow>(BatchConstants.VIDEO_DAILY_STATS+"JobFlow")
                .start(new JobStepBuilder(new StepBuilder(BatchConstants.VIDEO_DAILY_STATS+"Step", jobRepository))
                        .job(videoDailyStatsJob)
                        .build())
                .build();
    }

    // 영상 일별 정산 job flow
    @Bean
    public Flow revenueJobFlow(Job videoDailyRevenueJob) {
        return new FlowBuilder<Flow>(BatchConstants.VIDEO_DAILY_REVENUE+"JobFlow")
                .start(new JobStepBuilder(new StepBuilder(BatchConstants.VIDEO_DAILY_REVENUE+"Step", jobRepository))
                        .job(videoDailyRevenueJob)
                        .build())
                .build();
    }

    // 광고 일별 정산 job flow
    @Bean
    public Flow adRevenueJobFlow(Job adDailyRevenueJob) {
        return new FlowBuilder<Flow>(BatchConstants.AD_DAILY_REVENUE+"JobFlow")
                .start(new JobStepBuilder(new StepBuilder(BatchConstants.AD_DAILY_REVENUE+"Step", jobRepository))
                        .job(adDailyRevenueJob)
                        .build())
                .build();
    }
}
