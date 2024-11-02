package com.project.revenueservice.batch;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.VideoDto;
import com.project.revenueservice.entity.VideoCumulativeStats;
import com.project.revenueservice.repository.VideoCumulativeStatsRepository;
import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
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
   // private final EntityManagerFactory entityManagerFactory;

    private final StreamingServiceClient streamingClient;
    //private final VideoPagingRepository videoRepository;
    private final VideoCumulativeStatsRepository videoCumulativeStatsRepository;

    private final int chunkSize = 10;

    @Bean
    public Job videoCumulativeJob() {
        log.info("동영상 - 누적 N일차 배치 시작");

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

//    @Bean
//    public JpaPagingItemReader<UserVideoHistoryBatchDto> watchTimeReader() {
//        log.info("watchTimeReader");
//
//        return new JpaPagingItemReaderBuilder<UserVideoHistoryBatchDto>()
//                .name("watchTimeReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT new com.project.revenueservice.dto.UserVideoHistoryBatchDto(u.videoId, SUM(u.watchTime)) " +
//                        "FROM UserVideoHistory u GROUP BY u.video.id")
//                .pageSize(chunkSize)
//                .build();
//
//    }
        @Bean
        public ItemReader<UserVideoHistoryBatchDto> watchTimeReader() {
            return new ItemReader<>() {
                private Iterator<UserVideoHistoryBatchDto> iterator;

                @Override
                public UserVideoHistoryBatchDto read() throws Exception {
                    if(iterator == null) {
                        List<UserVideoHistoryBatchDto> videos = streamingClient.getTotalWatchTimeByVideo();
                        iterator = videos.iterator();
                    }

                    return iterator != null && iterator.hasNext() ? iterator.next() : null;
                }
            };

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

                //Video video = videoRepository.findById(userVideoHistory.getVideoId());

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
                .<VideoDto, VideoCumulativeStats> chunk(chunkSize, transactionManager)
                .reader(viewsReader())
                .processor(viewsProcessor())
                .writer(viewsWriter())
                .build();
    }


//    @Bean
//    public RepositoryItemReader<Video> viewsReader() {
//        log.info("views Reader");
//
//        return new RepositoryItemReaderBuilder<Video>()
//                .name("viewsReader")
//                .pageSize(chunkSize)
//                .methodName("findAll")
//                .repository(videoRepository)
//                .sorts(Map.of("id", Sort.Direction.ASC))
//                .build();
//
//    }

    @Bean
    public ItemReader<VideoDto> viewsReader() {
        return new ItemReader<>() {
            private Iterator<VideoDto> iterator;

            @Override
            public VideoDto read() throws Exception {
                if(iterator == null) {
                    List<VideoDto> videos = streamingClient.getAllVideos();
                    iterator = videos.iterator();
                }

                return iterator != null && iterator.hasNext() ? iterator.next() : null;
            }
        };

    }

    @Bean
    public ItemProcessor<VideoDto, VideoCumulativeStats> viewsProcessor() {
        log.info("view Processor");

        return new ItemProcessor<VideoDto, VideoCumulativeStats>() {

            @Override
            public VideoCumulativeStats process(VideoDto item) throws Exception {
                return VideoCumulativeStats.builder()
                        .videoId(item.getId())
                        .cumulativeViews(item.getViews())
                        .cumulativeWatchTime(0)
                        .build();
            }
        };
    }

    @Bean
    public RepositoryItemWriter<VideoCumulativeStats> viewsWriter() {
        log.info("동영상 - 누적 N일차 배치 끝---");
        return new RepositoryItemWriterBuilder<VideoCumulativeStats>()
                .repository(videoCumulativeStatsRepository)
                .methodName("save")
                .build();
    }
}
