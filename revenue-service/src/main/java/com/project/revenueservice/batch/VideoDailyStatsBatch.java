package com.project.revenueservice.batch;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import com.project.revenueservice.dto.VideoDto;
import com.project.revenueservice.entity.VideoCumulativeStats;
import com.project.revenueservice.entity.VideoDailyStats;
import com.project.revenueservice.repository.VideoCumulativeStatsRepository;
import com.project.revenueservice.repository.VideoDailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j(topic = "동영상 일별 통계")
@Configuration
@RequiredArgsConstructor
public class VideoDailyStatsBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final VideoDailyStatsRepository dailyStatsRepository;
    private final VideoCumulativeStatsRepository videoCumulativeStatsRepository;

    private final StreamingServiceClient streamingClient;

    private int chunkSize = 10;

    @Bean
    public Job videoDailyStatsJob() throws ParseException {
        log.info("동영상 - 일별 통계 배치 시작");

        return new JobBuilder("videoDailyStatsJob", jobRepository)
                .start(videoViewsTaskletStep())
                .next(calculateDiffViewsStep())
                .next(videoWatchTimeTaskletStep())
                .next(calculateDiffWatchTimeStep())
                .build();
    }

    // videoViewsTaskletStep : 동영상 목록(동영상 ID, 조회수) 조회하여 Step ExecutionContext에 저장
    @Bean
    public Step videoViewsTaskletStep() {
        return new StepBuilder("videoViewsTaskletStep", jobRepository)
                .tasklet(videoViewsTasklet(), transactionManager)
                .listener(viewsPromotionListener())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener viewsPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"videoViewsList"}); // 자동으로 승격시킬 키 목록
        return listener;
    }


    @Bean
    public Tasklet videoViewsTasklet() {
        return (contribution, chunkContext) -> {
            List<VideoDto> videoViewsList = streamingClient.getAllVideos();
            ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            stepContext.put("videoViewsList", videoViewsList);
            return RepeatStatus.FINISHED;
        };
    }

    // calculateDiffViewsStep : 누적 N일차 조회수 - 누적 N-1일차 조회수를 통해 일별 조회수 계산하여 업데이트
    @Bean
    public Step calculateDiffViewsStep() throws ParseException {
        log.info("calculateDiffViewsStep");

        return new StepBuilder("calculateDiffViewsStep", jobRepository)
                .<VideoCumulativeStats, VideoDailyStats>chunk(chunkSize, transactionManager)
                .reader(getPreviousDayCumulativeReader(null))
                .processor(viewsDiffProcessor(null))
                .writer(viewsDiffWriter())
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
        List<Long> videoIdList = streamingClient.getAllVideoIds();


        return new RepositoryItemReaderBuilder<VideoCumulativeStats>()
                .name("getPreviousDayCumulativeReader")
                .repository(videoCumulativeStatsRepository)
                .methodName("findByCreatedAtAndVideoIn")
                .arguments(parsedDate, videoIdList)
                .pageSize(chunkSize)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();

    }

    // 조회수 차이 계산
    @Bean
    @StepScope
    public ItemProcessor<VideoCumulativeStats, VideoDailyStats> viewsDiffProcessor(
            @Value("#{jobExecutionContext[videoViewsList]}") List<VideoDto> videoViewsList) {
        return new ItemProcessor<VideoCumulativeStats, VideoDailyStats>() {
            private Iterator<Long> idIterator;

            @Override
            public VideoDailyStats process(VideoCumulativeStats item) throws Exception {
                if(idIterator == null) { // videoId 기준으로 order by를 사용하지 않기 위한 것
                    Set<Long> uniqueIds = new HashSet<>();
                    for(VideoDto dto : videoViewsList) {
                        uniqueIds.add(dto.getId());
                    }
                    uniqueIds.add(item.getVideoId());
                    idIterator = uniqueIds.iterator();
                }

                if(!idIterator.hasNext()) {
                    return null;
                }

                Long targetVideoId = idIterator.next();
                for(VideoDto dto : videoViewsList) {
                    if(dto.getId().equals(targetVideoId)) {
                        long diff = dto.getViews() - item.getCumulativeViews(); // 일별 조회수 계산

                        return VideoDailyStats.builder()
                                .videoId(dto.getId())
                                .dailyViews(diff)
                                .dailyWatchTime(0) // 재생 시간은 아직 알지 못하므로 0으로 생성
                                .build();
                    }
                }
                return null;

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

    // videoWatchTimeTaskletStep : 재생내역에서 동영상 별로 재생 시간 합산(SUM, GROUP BY)한 후 조회하여 Step ExecutionContext에 저장
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
            List<UserVideoHistoryBatchDto> videoWatchTimeList = streamingClient.getVideoByDay();
            ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            stepContext.put("videoWatchTimeList", videoWatchTimeList);
            return RepeatStatus.FINISHED;
        };
    }

    // calculateDiffWatchTimeStep : 누적 N일차 재생시간 - 누적 N-1일차 재생시간을 통해 일별 재생시간 계산하여 업데이트
    @Bean
    public Step calculateDiffWatchTimeStep() throws ParseException {
        log.info("calculateDiffWatchTimeStep");

        return new StepBuilder("calculateDiffWatchTimeStep", jobRepository)
                .<VideoCumulativeStats, VideoDailyStats>chunk(chunkSize, transactionManager)
                .reader(getPreviousDayWatchTimeCumulativeReader(null))
                .processor(watchTimeDiffProcessor(null, null))
                .writer(watchTimeDiffWriter())
                .build();
    }

    // 일별 (N-1일차 누적 재생시간 : 누적 테이블에서 N-1일차 누적 재생시간 가져오기)
    @Bean
    @StepScope
    public RepositoryItemReader<VideoCumulativeStats> getPreviousDayWatchTimeCumulativeReader(
            @Value("#{jobParameters[currentDate]}") String currentDate) throws ParseException { // 스케줄링 실행할 때 입력받은 날짜 전달 받음

        // String을 Date로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter).minusDays(1);

        // 동영상 ID 리스트 가져오기
        List<Long> videoIdList = streamingClient.getLatestVideos();

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
            @Value("#{jobExecutionContext[videoWatchTimeList]}") List<UserVideoHistoryBatchDto> videoWatchTimeList, // step executionContext를 job executionContext로 승격시켜 전달 받음
            @Value("#{jobParameters[currentDate]}") String currentDate) {

        return new ItemProcessor<VideoCumulativeStats, VideoDailyStats>() {
            private Iterator<Long> idIterator;

            @Override
            public VideoDailyStats process(VideoCumulativeStats item) throws Exception {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter);

                if(idIterator == null) {
                    // 재생 내역이 현재 값이기 때문에 동영상 ID 기준을 해당 테이블로 잡음
                    Set<Long> uniqueIds = new HashSet<>();
                    for(UserVideoHistoryBatchDto dto : videoWatchTimeList) {
                        uniqueIds.add(dto.getVideoId());
                    }
                    uniqueIds.add(item.getVideoId());
                    idIterator = uniqueIds.iterator();
                }

                if (!idIterator.hasNext()) {
                    return null;
                }

                // 다음 Video ID 가져오기
                Long targetVideoId = idIterator.next();
                for (UserVideoHistoryBatchDto dto : videoWatchTimeList) {
                    if (dto.getVideoId().equals(targetVideoId)) {
                        long watchTimeDiff = dto.getWatchTime() - item.getCumulativeWatchTime(); // 일별 재생 시간 계산

                        VideoDailyStats dailyStats = dailyStatsRepository.findByVideoIdAndCreatedAt(targetVideoId, parsedDate);
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
}
