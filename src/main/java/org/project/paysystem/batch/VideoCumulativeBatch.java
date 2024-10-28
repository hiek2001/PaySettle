package org.project.paysystem.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.paysystem.dto.UserVideoHistoryBatchDto;
import org.project.paysystem.entity.Video;
import org.project.paysystem.entity.VideoCumulativeStats;
import org.project.paysystem.repository.VideoCumulativeStatsRepository;
import org.project.paysystem.repository.VideoPagingRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Slf4j(topic = "동영상 누적 조회수 N일차 생성 배치")
@Configuration
@RequiredArgsConstructor
public class VideoCumulativeBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;


    private final VideoPagingRepository videoRepository;
    private final VideoCumulativeStatsRepository videoCumulativeStatsRepository;

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
                .<UserVideoHistoryBatchDto, VideoCumulativeStats> chunk(chunkSize, transactionManager)
                .reader(watchTimeReader())
                .processor(watchTimeProcessor(null))
                .writer(watchTimeWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<UserVideoHistoryBatchDto> watchTimeReader() {
        log.info("watchTimeReader");

        return new JpaPagingItemReaderBuilder<UserVideoHistoryBatchDto>()
                .name("watchTimeReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT new org.project.paysystem.dto.UserVideoHistoryBatchDto(u.video.id, SUM(u.watchTime)) " +
                        "FROM UserVideoHistory u GROUP BY u.video.id")
                .pageSize(chunkSize)
                .build();

    }


    @Bean
    @StepScope
    public ItemProcessor<UserVideoHistoryBatchDto, VideoCumulativeStats> watchTimeProcessor(
            @Value("#{jobParameters[currentDate]}") String currentDate) {
        log.info("watchTimeProcessor");

        return new ItemProcessor<UserVideoHistoryBatchDto, VideoCumulativeStats>() {

            @Override
            public VideoCumulativeStats process(UserVideoHistoryBatchDto userVideoHistory) throws Exception {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10));

                Video video = videoRepository.findById(userVideoHistory.getVideoId());

                Optional<VideoCumulativeStats> optionalStats = videoCumulativeStatsRepository.findByVideoIdAndCreatedAt(video.getId(), parsedDate);
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
