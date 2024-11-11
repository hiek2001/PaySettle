package com.project.revenueservice.batch;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import com.project.revenueservice.dto.VideoDto;
import com.project.revenueservice.entity.VideoCumulativeStats;
import com.project.revenueservice.repository.VideoCumulativeStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "동영상 누적 조회수 N일차 생성 배치")
@Configuration
@RequiredArgsConstructor
public class VideoCumulativeBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StreamingServiceClient streamingClient;
    private final VideoCumulativeStatsRepository videoCumulativeStatsRepository;

    @Value("${spring.batch.chunksize}")
    private int chunkSize;

    @Bean
    public Job videoCumulativeJob() {
        return new JobBuilder(BatchConstants.VIDEO_CUMULATIVE+"Job", jobRepository)
                .start(viewsStep())
                .next(watchTimeStep())
                .build();
    }

    // viewStep : 동영상 별로 동영상 조회수 집계할 누적 N일차 VideoCumulativeStats 객체 생성
    @Bean
    public Step viewsStep() {
        return new StepBuilder("viewsStep", jobRepository)
                .<VideoDto, VideoCumulativeStats> chunk(chunkSize, transactionManager)
                .reader(viewsReader())
                .processor(viewsProcessor())
                .writer(viewsWriter())
                .build();
    }

    @Bean
    public ItemReader<VideoDto> viewsReader() {
        return new ItemReader<>() {
            private Iterator<VideoDto> iterator;

            @Override
            public VideoDto read() throws Exception {
                if(iterator == null) {
                    List<VideoDto> videos = streamingClient.getAllVideos(); // 스트리밍 서비스의 동영상 데이터 조회 API 호출
                    iterator = videos.iterator();
                }

                return iterator != null && iterator.hasNext() ? iterator.next() : null;
            }
        };

    }

    @Bean
    public ItemProcessor<VideoDto, VideoCumulativeStats> viewsProcessor() {
        return new ItemProcessor<VideoDto, VideoCumulativeStats>() {

            @Override
            public VideoCumulativeStats process(VideoDto item) throws Exception {
                return VideoCumulativeStats.builder()
                        .videoId(item.getId())
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

    // watchTimeStep : 재생 내역을 바탕으로 동영상 별 재생 시간을 집계할 누적 N일차 VideoCumulativeStats 객체 생성
    @Bean
    public Step watchTimeStep() {
        return new StepBuilder("watchTimeStep", jobRepository)
                .<UserVideoHistoryBatchDto, VideoCumulativeStats> chunk(chunkSize, transactionManager)
                .reader(watchTimeReader())
                .processor(watchTimeProcessor(null))
                .writer(watchTimeWriter())
                .build();
    }
        @Bean
        public ItemReader<UserVideoHistoryBatchDto> watchTimeReader() {
            return new ItemReader<>() {
                private Iterator<UserVideoHistoryBatchDto> iterator;

                @Override
                public UserVideoHistoryBatchDto read() throws Exception {
                    if(iterator == null) {
                        List<UserVideoHistoryBatchDto> videos = streamingClient.getTotalWatchTimeByVideo(); // 스트리밍 서비스의 동영상 재생 내역 조회 API 호출
                        iterator = videos.iterator();
                    }

                    return iterator != null && iterator.hasNext() ? iterator.next() : null;
                }
            };

        }



    @Bean
    @StepScope
    public ItemProcessor<UserVideoHistoryBatchDto, VideoCumulativeStats> watchTimeProcessor(
            @Value("#{jobParameters["+ BatchKeys.CURRENT_DATE +"]}") String currentDate) {
        return new ItemProcessor<UserVideoHistoryBatchDto, VideoCumulativeStats>() {

            @Override
            public VideoCumulativeStats process(UserVideoHistoryBatchDto userVideoHistory) throws Exception {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10));

                Optional<VideoCumulativeStats> optionalStats = videoCumulativeStatsRepository.findByVideoIdAndCreatedAt(userVideoHistory.getVideoId(), parsedDate);
                if (optionalStats.isPresent()) { // 존재하면 업데이트
                    VideoCumulativeStats stats = optionalStats.get();
                    stats.updateCumulativeWatchTime(userVideoHistory.getWatchTime());
                    return stats;
                } else {
                    // 존재하지 않으면 새로운 객체 생성
                    return VideoCumulativeStats.builder()
                            .videoId(userVideoHistory.getVideoId())
                            .cumulativeViews(0)
                            .cumulativeWatchTime(userVideoHistory.getWatchTime())
                            .build();
                }
            }
        };

    }

    @Bean
    public ItemWriter<VideoCumulativeStats> watchTimeWriter() {
        return new RepositoryItemWriterBuilder<VideoCumulativeStats>()
                .repository(videoCumulativeStatsRepository)
                .methodName("save")
                .build();

    }
}
