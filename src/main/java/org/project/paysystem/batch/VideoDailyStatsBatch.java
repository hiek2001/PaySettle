package org.project.paysystem.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.project.paysystem.dto.UserVideoHistoryBatchDto;
import org.project.paysystem.entity.UserVideoHistory;
import org.project.paysystem.entity.Video;
import org.project.paysystem.entity.VideoCumulativeStats;
import org.project.paysystem.entity.VideoDailyStats;
import org.project.paysystem.repository.UserVideoHistoryRepository;
import org.project.paysystem.repository.VideoCumulativeStatsRepository;
import org.project.paysystem.repository.VideoDailyStatsRepository;
import org.project.paysystem.repository.VideoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "동영상 일별 통계 테이블 배치")
@Configuration
@RequiredArgsConstructor
public class VideoDailyStatsBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final VideoRepository videoRepository;
    private final VideoDailyStatsRepository dailyStatsRepository;
    private final VideoCumulativeStatsRepository videoCumulativeStatsRepository;
    private final UserVideoHistoryRepository userVideoHistoryRepository;

    private int chunkSize = 10;

    @Bean
    public Job videoDailyStatsJob() {
        log.info("videoDailyStats Job");

        return new JobBuilder("videoDailyStatsJob", jobRepository)
                .start(calculateDiffViewsStep())
                .next(calculateDiffWatchTimeStep())
                .build();
    }

    // 일별 재생 시간
    @Bean
    public Step calculateDiffWatchTimeStep() {
        log.info("calculateDiffWatchTimeStep");

        return new StepBuilder("calculateDiffWatchTimeStep", jobRepository)
                .<Pair<UserVideoHistory, VideoCumulativeStats>, VideoDailyStats>chunk(chunkSize, transactionManager)
                .reader(watchTimeMultiReader(null))
                .processor(watchTimeDiffProcessor())
                .writer(watchTimeDiffWriter())
                .build();
    }

    // 일별 (N일차 누적 재생 시간 : 현재 누적 재생 시간 필요)
    @Bean
    @StepScope
    public RepositoryItemReader<UserVideoHistory> getNDayWatchTimeCumulativeReader() {
        log.info("getNDayWatchTimeCumulativeReader");

        return new RepositoryItemReaderBuilder<UserVideoHistory>()
                .name("getNDayWatchTimeCumulativeReader")
                .pageSize(chunkSize)
                .methodName("findLatestHistoryByVideo")
                .repository(userVideoHistoryRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

    }

    // 일별 (N-1일차 누적 재생시간 : 누적 테이블에서 N-1일차 누적 재생시간 가져오기)
    @Bean
    @StepScope
    public RepositoryItemReader<VideoCumulativeStats> getPreviousDayWatchTimeCumulativeReader(@Value("#{jobParameters[currentDate]}") String currentDate) throws ParseException {
        // String을 Date로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter);

        // 동영상 ID 리스트 가져오기
        List<Long> videoIdList = userVideoHistoryRepository.findLatestHistoryByIds();

        return new RepositoryItemReaderBuilder<VideoCumulativeStats>()
                .name("getPreviousDayWatchTimeCumulativeReader")
                .repository(videoCumulativeStatsRepository)
                .methodName("findByCreatedAtAndVideoIn")
                .arguments(parsedDate, videoIdList)
                .pageSize(chunkSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

    }

    @Bean
    @StepScope
    public ItemReader<Pair<UserVideoHistory, VideoCumulativeStats>> watchTimeMultiReader(@Value("#{jobParameters[currentDate]}") String currentDate) {

        return new ItemReader<Pair<UserVideoHistory, VideoCumulativeStats>>() {
            @Override
            public Pair<UserVideoHistory, VideoCumulativeStats> read() throws Exception {
                UserVideoHistory NDayVideo = getNDayWatchTimeCumulativeReader().read();
                VideoCumulativeStats periousDayVideo = getPreviousDayWatchTimeCumulativeReader(currentDate).read();

                if (NDayVideo != null && periousDayVideo != null && NDayVideo.getVideo().getId().equals(periousDayVideo.getVideo().getId())) {
                    return Pair.of(NDayVideo, periousDayVideo);  // 두 Reader의 데이터를 Pair로 묶음
                } else {
                    return null;  // 더 이상 읽을 데이터가 없으면 null 리턴
                }
            }
        };
    }

    // 재생시간 차이 계산
    @Bean
    @StepScope
    public ItemProcessor<Pair<UserVideoHistory, VideoCumulativeStats>, VideoDailyStats> watchTimeDiffProcessor() {
        return new ItemProcessor<Pair<UserVideoHistory, VideoCumulativeStats>, VideoDailyStats>() {

            @Override
            public VideoDailyStats process(Pair<UserVideoHistory, VideoCumulativeStats> pair) throws Exception {
                UserVideoHistory NDayVideo = pair.getLeft();
                VideoCumulativeStats peviousDayVideo = pair.getRight();

                long diff = NDayVideo.getWatchTime() - peviousDayVideo.getCumulativeWatchTime();

                VideoDailyStats dailyStats = dailyStatsRepository.findByVideoId(NDayVideo.getVideo().getId());
                if(dailyStats == null) {
                    log.warn("VideoDailyStats에 해당 VideoId가 없습니다: "+NDayVideo.getVideo().getId());
                    return null; // 없으면 그냥 넘어가게 설정
                }

                dailyStats.updateDailyWatchTime(diff);
                return dailyStats;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<VideoDailyStats> watchTimeDiffWriter() {
        return new RepositoryItemWriterBuilder<VideoDailyStats>()
                .repository(dailyStatsRepository)
                .methodName("save")
                .build();
    }

    // 일별 조회수
    @Bean
    public Step calculateDiffViewsStep() {
        log.info("calculateDiffViewsStep");

        return new StepBuilder("calculateDiffViewsStep", jobRepository)
                .<Pair<Video, VideoCumulativeStats>, VideoDailyStats>chunk(chunkSize, transactionManager)
                .reader(multiReader(null))
                .processor(viewsDiffProcessor())
                .writer(viewsDiffWriter())
                .build();
    }

    // 일별 (N일차 누적 조회수 : 현재 누적 조회수 필요)
    @Bean
    @StepScope
    public RepositoryItemReader<Video> getNDayCumulativeReader() {
        log.info("views Reader");

        return new RepositoryItemReaderBuilder<Video>()
                .name("getNDayCumulativeReader")
                .pageSize(chunkSize)
                .methodName("findAll")
                .repository(videoRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    // 일별 (N-1일차 누적 조회수 : 누적 테이블에서 N-1일차 누적 조회수 가져오기)
    @Bean
    @StepScope
    public RepositoryItemReader<VideoCumulativeStats> getPreviousDayCumulativeReader(@Value("#{jobParameters[currentDate]}") String currentDate) throws ParseException {
        // String을 Date로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter);

        // 동영상 ID 리스트 가져오기
        List<Long> videoIdList = videoRepository.findAllIds();

        return new RepositoryItemReaderBuilder<VideoCumulativeStats>()
                .name("getPreviousDayCumulativeReader")
                .repository(videoCumulativeStatsRepository)
                .methodName("findByCreatedAtAndVideoIn")
                .arguments(parsedDate, videoIdList)
                .pageSize(chunkSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

    }


    @Bean
    @StepScope
    public ItemReader<Pair<Video, VideoCumulativeStats>> multiReader(@Value("#{jobParameters[currentDate]}") String currentDate) {

        return new ItemReader<Pair<Video, VideoCumulativeStats>>() {
            @Override
            public Pair<Video, VideoCumulativeStats> read() throws Exception {
                Video NDayVideo = getNDayCumulativeReader().read();
                VideoCumulativeStats periousDayVideo = getPreviousDayCumulativeReader(currentDate).read();

                if (NDayVideo != null && periousDayVideo != null && NDayVideo.getId().equals(periousDayVideo.getVideo().getId())) {
                    return Pair.of(NDayVideo, periousDayVideo);  // 두 Reader의 데이터를 Pair로 묶음
                } else {
                    return null;  // 더 이상 읽을 데이터가 없으면 null 리턴
                }
            }
        };
    }

    // 조회수 차이 계산
    @Bean
    @StepScope
    public ItemProcessor<Pair<Video, VideoCumulativeStats>, VideoDailyStats> viewsDiffProcessor() {
        return new ItemProcessor<Pair<Video, VideoCumulativeStats>, VideoDailyStats>() {

            @Override
            public VideoDailyStats process(Pair<Video, VideoCumulativeStats> pair) throws Exception {
                Video NDayVideo = pair.getLeft();
                VideoCumulativeStats peviousDayVideo = pair.getRight();

                long diff = NDayVideo.getVideoViews() - peviousDayVideo.getCumulativeViews();
                return VideoDailyStats.builder()
                        .video(NDayVideo)
                        .dailyViews(diff)
                        .dailyWatchTime(0)
                        .build();
            }
        };
    }

    @Bean
    public RepositoryItemWriter<VideoDailyStats> viewsDiffWriter() {
        return new RepositoryItemWriterBuilder<VideoDailyStats>()
                .repository(dailyStatsRepository)
                .methodName("save")
                .build();
    }
}
