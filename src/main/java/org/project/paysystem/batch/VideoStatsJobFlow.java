package org.project.paysystem.batch;

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
    private final VideoStatsJobDecider jobCompletionDecider;

    @Bean
    public Job combinedJob() {
        return new JobBuilder("combinedJob", jobRepository)
                .start(cumulativeJobFlow(videoCumulativeJob))  // 첫 번째 Job 시작
                .next(jobCompletionDecider)  // Decider로 상태 확인
                .on("*")
                .to(dailyStatsJobFlow(videoDailyStatsJob)) // 첫 번째 Job이 완료되면 두 번째 Job 실행
                .end()  // Job 종료
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

}
