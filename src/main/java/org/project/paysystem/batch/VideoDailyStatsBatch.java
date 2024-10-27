package org.project.paysystem.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.project.paysystem.dto.UserVideoHistoryBatchDto;
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
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public Job videoDailyStatsJob() throws ParseException {
        log.info("videoDailyStats Job");

        return new JobBuilder("videoDailyStatsJob", jobRepository)
                .start(calculateDiffViewsStep())
                .next(videoWatchTimeTaskletStep())
                .next(calculateDiffWatchTimeStep())
                .build();
    }

    @Bean
    public Step videoWatchTimeTaskletStep() {
        return new StepBuilder("videoWatchTimeTaskletStep", jobRepository)
                .tasklet(videoWatchTimeTasklet(), transactionManager)
                .listener(promotionListener())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"videoWatchTimeList"}); // 자동으로 승격시킬 키 목록
        return listener;
    }


    @Bean
    public Tasklet videoWatchTimeTasklet() {
        return (contribution, chunkContext) -> {
            List<UserVideoHistoryBatchDto> videoWatchTimeList = userVideoHistoryRepository.findTodayWatchTime();
            ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            stepContext.put("videoWatchTimeList", videoWatchTimeList);
            return RepeatStatus.FINISHED;
        };
    }

    // 일별 재생 시간
    @Bean
    public Step calculateDiffWatchTimeStep() throws ParseException {
        log.info("calculateDiffWatchTimeStep");

        return new StepBuilder("calculateDiffWatchTimeStep", jobRepository)
                .<VideoCumulativeStats, VideoDailyStats>chunk(chunkSize, transactionManager)
                .reader(getPreviousDayWatchTimeCumulativeReader(null))
                .processor(watchTimeDiffProcessor(null))
                .writer(watchTimeDiffWriter())
                .build();
    }

    // 일별 (N-1일차 누적 재생시간 : 누적 테이블에서 N-1일차 누적 재생시간 가져오기)
    @Bean
    @StepScope
    public RepositoryItemReader<VideoCumulativeStats> getPreviousDayWatchTimeCumulativeReader(
            @Value("#{jobParameters[currentDate]}") String currentDate) throws ParseException {

        // String을 Date로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter).minusDays(1);

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

    // 재생시간 차이 계산, 일별 재생 시간을 업데이트
    @Bean
    @StepScope
    public ItemProcessor<VideoCumulativeStats, VideoDailyStats> watchTimeDiffProcessor(
            @Value("#{jobExecutionContext[videoWatchTimeList]}") List<UserVideoHistoryBatchDto> videoWatchTimeList) {

        return new ItemProcessor<VideoCumulativeStats, VideoDailyStats>() {
            private Iterator<Long> idIterator;

            @Override
            public VideoDailyStats process(VideoCumulativeStats item) throws Exception {
                if(idIterator == null) {
                    // 재생 내역이 현재 값이기 때문에 동영상 ID 기준을 해당 테이블로 잡음
                    Set<Long> uniqueIds = new HashSet<>();
                    for(UserVideoHistoryBatchDto dto : videoWatchTimeList) {
                        uniqueIds.add(dto.getVideoId());
                    }
                    uniqueIds.add(item.getVideo().getId());
                    idIterator = uniqueIds.iterator();
                }

                if (!idIterator.hasNext()) {
                    return null;
                }

                // 다음 Video ID 가져오기
                Long targetVideoId = idIterator.next();
                for (UserVideoHistoryBatchDto dto : videoWatchTimeList) {
                    if (dto.getVideoId().equals(targetVideoId)) {
                        long watchTimeDiff = dto.getWatchTime() - item.getCumulativeWatchTime();

                        VideoDailyStats dailyStats = dailyStatsRepository.findByVideoId(targetVideoId);
                        dailyStats.updateDailyWatchTime(watchTimeDiff);
                        return dailyStats;
                    }
                }

                return null; // 매칭되는 것이 없다면 null을 반환하여 넘어가게 설정
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
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter).minusDays(1);

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
