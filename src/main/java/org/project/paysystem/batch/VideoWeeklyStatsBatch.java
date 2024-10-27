package org.project.paysystem.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.paysystem.dto.VideoDailyStatusBatchDto;
import org.project.paysystem.entity.Video;
import org.project.paysystem.entity.VideoWeeklyStats;
import org.project.paysystem.repository.VideoRepository;
import org.project.paysystem.repository.VideoWeeklyStatsRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j(topic = "동영상 주간 통계")
@Configuration
@RequiredArgsConstructor
public class VideoWeeklyStatsBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final VideoRepository videoRepository;
    private final VideoWeeklyStatsRepository weeklyStatsRepository;

    private int chunkSize = 10;

    @Bean
    public Job videoWeeklyStatsJob() {
        log.info("videoWeeklyStatsJob");

        return new JobBuilder("videoWeeklyStatsJob", jobRepository)
                .start(weeklyStatsStep())
                .build();

    }

    // 조회수, 재생 시간
    @Bean
    public Step weeklyStatsStep() {
        log.info("weeklyStatsStep ");

        return new StepBuilder("weeklyViewsStep", jobRepository)
                .<VideoDailyStatusBatchDto, VideoWeeklyStats> chunk(chunkSize, transactionManager)
                .reader(getDailyStatsReader(null))
                .processor(weeklyStatsProcessor())
                .writer(weeklyStatsWriter())
                .build();
    }


    // 일별 조회수, 재생 시간에서 7일 합산하여 가져오기
    @Bean
    @StepScope
    public JpaPagingItemReader<VideoDailyStatusBatchDto> getDailyStatsReader(
            @Value("#{jobParameters[currentDate]}") String currentDate) {

        log.info("getDailyStatsReader");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate = LocalDate.parse(currentDate.substring(0, 10), formatter);
        LocalDate startDate = LocalDate.parse(currentDate.substring(0, 10), formatter).minusDays(6);

        return new JpaPagingItemReaderBuilder<VideoDailyStatusBatchDto>()
                .name("getDailyStatsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT new org.project.paysystem.dto.VideoDailyStatusBatchDto(vds.video.id, SUM(vds.dailyViews), SUM(vds.dailyWatchTime)) FROM VideoDailyStats vds " +
                        "WHERE vds.createdAt BETWEEN :startDate AND :endDate GROUP BY vds.video.id")
                .parameterValues(Map.of("startDate", startDate, "endDate", endDate))
                .pageSize(chunkSize)
                .build();
    }

    // 주간
    @Bean
    public ItemProcessor<VideoDailyStatusBatchDto, VideoWeeklyStats> weeklyStatsProcessor() {
        log.info("weeklyStatsProcessor");

        return new ItemProcessor<VideoDailyStatusBatchDto, VideoWeeklyStats>() {

            @Override
            public VideoWeeklyStats process(VideoDailyStatusBatchDto item) throws Exception {
                Video video = videoRepository.batchFindById(item.getVideoId());

                return VideoWeeklyStats.builder()
                        .video(video)
                        .weeklyViews(item.getDailyViews())
                        .weeklyWatchTime(item.getDailyWatchTime())
                        .build();
            }
        };
    }
    @Bean
    public RepositoryItemWriter<VideoWeeklyStats> weeklyStatsWriter() {
        return new RepositoryItemWriterBuilder<VideoWeeklyStats>()
                .repository(weeklyStatsRepository)
                .methodName("save")
                .build();
    }

}
