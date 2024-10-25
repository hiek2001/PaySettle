package org.project.paysystem.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.paysystem.dto.UserVideoHistoryBatchDto;
import org.project.paysystem.entity.UserVideoHistory;
import org.project.paysystem.entity.Video;
import org.project.paysystem.entity.VideoCumulativeStats;
import org.project.paysystem.repository.UserVideoHistoryRepository;
import org.project.paysystem.repository.VideoCumulativeStatsRepository;
import org.project.paysystem.repository.VideoPagingRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;


import java.util.Map;
import java.util.Optional;

@Slf4j(topic = "동영상 누적 조회수 N일차 생성 배치")
@Configuration
@RequiredArgsConstructor
public class VideoCumulativeBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final VideoPagingRepository videoRepository;
    private final VideoCumulativeStatsRepository videoCumulativeStatsRepository;
    private final UserVideoHistoryRepository userVideoHistoryRepository;

    private final int chunkSize = 10;

    @Bean
    public Job videoCumulativeJob() {
        log.info("videoCumulative Job");

        return new JobBuilder("videoCumulativeJob", jobRepository)
                .start(viewsStep())
                .next(watchTimeStep())
                .build();
    }

    // 누적 재생 시간
    @Bean
    public Step watchTimeStep() {
        log.info("watchTimeStep");

        return new StepBuilder("watchTimeStep", jobRepository)
                .<UserVideoHistory, VideoCumulativeStats> chunk(chunkSize, transactionManager)
                .reader(watchTimeReader())
                .processor(watchTimeProcessor())
                .writer(watchTimeWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<UserVideoHistory> watchTimeReader() {
        log.info("watchTimeReader");

        return new RepositoryItemReaderBuilder<UserVideoHistory>()
                .name("watchTimeReader")
                .pageSize(chunkSize)
                .methodName("findLatestHistoryByVideo")
                .repository(userVideoHistoryRepository)
                .sorts(Map.of("id", Sort.Direction.DESC))
                .build();

    }

    @Bean
    public ItemProcessor<UserVideoHistory, VideoCumulativeStats> watchTimeProcessor() {
        log.info("watchTimeProcessor");

        return new ItemProcessor<UserVideoHistory, VideoCumulativeStats>() {

            @Override
            public VideoCumulativeStats process(UserVideoHistory userVideoHistory) throws Exception {
                Video video = userVideoHistory.getVideo();

                Optional<VideoCumulativeStats> optionalStats = videoCumulativeStatsRepository.findByVideoId(video.getId());
                if (optionalStats.isPresent()) { // 존재하면 업데이트
                    VideoCumulativeStats stats = optionalStats.get();
                    stats.updateCumulativeWatchTime(userVideoHistory.getWatchTime());
                    return stats;
                } else {
                    // 존재하지 않으면 새로운 객체 생성
                    return VideoCumulativeStats.builder()
                            .video(video)
                            .cumulativeViews(0)
                            .cumulativeWatchTime(userVideoHistory.getWatchTime())
                            .build();
                }
            }
        };

    }

    @Bean
    public ItemWriter<VideoCumulativeStats> watchTimeWriter() {
        log.info("watchTimeWriter");

        return new RepositoryItemWriterBuilder<VideoCumulativeStats>()
                .repository(videoCumulativeStatsRepository)
                .methodName("save")
                .build();

    }

    // 누적 조회수
    @Bean
    public Step viewsStep() {
        log.info("views Step");

        return new StepBuilder("viewsStep", jobRepository)
                .<Video, VideoCumulativeStats> chunk(chunkSize, transactionManager)
                .reader(viewsReader())
                .processor(viewsProcessor())
                .writer(viewsWriter())
                .build();
    }


    @Bean
    public RepositoryItemReader<Video> viewsReader() {
        log.info("views Reader");

        return new RepositoryItemReaderBuilder<Video>()
                .name("viewsReader")
                .pageSize(chunkSize)
                .methodName("findAll")
                .repository(videoRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

    }

    @Bean
    public ItemProcessor<Video, VideoCumulativeStats> viewsProcessor() {
        log.info("view Processor");

        return new ItemProcessor<Video, VideoCumulativeStats>() {

            @Override
            public VideoCumulativeStats process(Video item) throws Exception {
                return VideoCumulativeStats.builder()
                        .video(item)
                        .cumulativeViews(item.getVideoViews())
                        .cumulativeWatchTime(0)
                        .build();
            }
        };
    }

    @Bean
    public RepositoryItemWriter<VideoCumulativeStats> viewsWriter() {

        return new RepositoryItemWriterBuilder<VideoCumulativeStats>()
                .repository(videoCumulativeStatsRepository)
                .methodName("save")
                .build();
    }
}
