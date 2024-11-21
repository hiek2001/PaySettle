package com.project.revenueservice.batch;

import com.project.revenueservice.batch.listener.ChunkExecutionTimeListener;
import com.project.revenueservice.batch.partitioner.VideoIdRangePartitioner;
import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.UserVideoHistoryBatchDto;
import com.project.revenueservice.dto.VideoDto;
import com.project.revenueservice.entity.VideoCumulativeStats;
import com.project.revenueservice.entity.VideoDailyStats;
import com.project.revenueservice.repository.VideoCumulativeStatsRepository;
import com.project.revenueservice.repository.VideoDailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
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

    private final ChunkExecutionTimeListener chunkExecutionListener;

    private final DataSource dataSource;

    @Value("${spring.batch.chunksize}")
    private int chunkSize;

    @Value("${spring.batch.partition.poolsize}")
    private int poolsize;



    @Bean
    public Job videoDailyStatsJob() throws Exception {
        return new JobBuilder(BatchConstants.VIDEO_DAILY_STATS+"Job", jobRepository)
                .start(viewsStepManager())
                .next(videoWatchTimeTaskletStep())
                .next(videoWatchTimeStep())
                .build();
    }

    // 일별 조회수 파티션 적용
    @Bean
    public TaskExecutorPartitionHandler partitionHandler() throws Exception {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(videoDailyStatsStep());
        partitionHandler.setTaskExecutor(viewsStepExecutor());
        partitionHandler.setGridSize(poolsize);
        return partitionHandler;
    }

    @Bean
    public TaskExecutor viewsStepExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolsize);
        executor.setMaxPoolSize(poolsize);
        executor.setThreadNamePrefix("partition-thread");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public Step viewsStepManager() throws Exception {
        return new StepBuilder("viewsStep.manager", jobRepository)
                .partitioner(BatchConstants.VIDEO_DAILY_STATS+"Step", partitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public VideoIdRangePartitioner partitioner() {
        return new VideoIdRangePartitioner(streamingClient);
    }

    // 일별 조회수
    @Bean
    public Step videoDailyStatsStep() throws Exception {
        return new StepBuilder(BatchConstants.VIDEO_DAILY_STATS+"Step", jobRepository)
                .<Pair<VideoDto, VideoCumulativeStats>, VideoDailyStats>chunk(chunkSize, transactionManager)
                .reader(multiReader(null, 0, 0))
                .processor(viewsDiffProcessor())
                .writer(viewsDiffWriter())
                .listener((StepExecutionListener) chunkExecutionListener) // 처리 시간 계산 logging
                .listener((ItemReadListener<? super Pair<VideoDto, VideoCumulativeStats>>) chunkExecutionListener)
                .listener((ItemProcessListener<? super Pair<VideoDto, VideoCumulativeStats>, ? super VideoDailyStats>) chunkExecutionListener)
                .build();
    }
    // 일별 (N일차 누적 조회수 : 현재 누적 조회수 필요)
    @Bean
    public ItemReader<VideoDto> viewsReader() {
        return new ItemReader<>() {
            private Iterator<VideoDto> iterator;
            private long lastId = 0;

            @Override
            public VideoDto read() throws Exception {
                if(iterator == null || !iterator.hasNext()) {

                    // zero-offSet 기법으로 데이터 요청 (lastId 이후의 데이터)
                    List<VideoDto> videos = streamingClient.getVideosAfterId(lastId, chunkSize);

                    if(videos.isEmpty()) { // 더 이상 가져올 데이터가 없으면 종료
                        return null;
                    }

                    iterator = videos.iterator();

                    lastId = videos.get(videos.size() - 1).getId();
                }
                return iterator != null && iterator.hasNext() ? iterator.next() : null;
            }
        };
    }
    // 일별 (N-1일차 누적 조회수 : 누적 테이블에서 N-1일차 누적 조회수 가져오기)
    @Bean
    @StepScope
    public JdbcPagingItemReader<VideoCumulativeStats> getPreviousDayCumulativeReader(
            @Value("#{jobParameters["+ BatchKeys.CURRENT_DATE +"]}") String currentDate,
            @Value("#{stepExecutionContext["+ BatchKeys.START_PAGE +"]}") int startPage,
            @Value("#{stepExecutionContext["+ BatchKeys.END_PAGE +"]}") int endPage
    ) throws Exception {
        // String을 Date로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter).minusDays(1);

        Map<String, Object> params = new HashMap<>();
        params.put("createdAt", parsedDate);
        params.put("startPage", startPage);
        params.put("endPage", endPage);

        JdbcPagingItemReader<VideoCumulativeStats> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(chunkSize);

        // RowMapper 설정
        reader.setRowMapper(new BeanPropertyRowMapper<>(VideoCumulativeStats.class));

        // 쿼리 프로바이더 설정
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT DISTINCT video_id, created_at, cumulative_views, cumulative_watch_time, id");
        queryProvider.setFromClause("FROM video_cumulative_stats");
        queryProvider.setWhereClause("WHERE created_at = :createdAt AND video_id BETWEEN :startPage AND :endPage");
        queryProvider.setSortKey("id");

        // QueryProvider를 JdbcPagingItemReader에 설정
        reader.setQueryProvider(queryProvider.getObject());

        // 파라미터 값 설정
        MapSqlParameterSource parameterValues = new MapSqlParameterSource();
        parameterValues.addValues(params);
        reader.setParameterValues(parameterValues.getValues());

        return reader;

    }

    @Bean
    @StepScope
    public ItemReader<Pair<VideoDto, VideoCumulativeStats>> multiReader(
            @Value("#{jobParameters["+BatchKeys.CURRENT_DATE+"]}") String currentDate,
            @Value("#{stepExecutionContext["+BatchKeys.START_PAGE+"]}") int startPage,
            @Value("#{stepExecutionContext["+BatchKeys.END_PAGE+"]}") int endPage
    ) throws Exception {
        return new ItemReader<Pair<VideoDto, VideoCumulativeStats>>() {

            @Override
            public Pair<VideoDto, VideoCumulativeStats> read() throws Exception {
                VideoDto videoDto = viewsReader().read();
                VideoCumulativeStats cumulativeStats = getPreviousDayCumulativeReader(currentDate, startPage, endPage).read();

                // 같은 videoId가 존재하는 경우 Pair로 묶어서 반환
                if (videoDto != null && cumulativeStats != null) {
                    return Pair.of(videoDto, cumulativeStats);
                }

                // 해당 videoId에 매칭되는 데이터가 없으면 null 반환
                return null;
            }
        };
    }

    // 조회수 차이 계산
    @Bean
    @StepScope
    public ItemProcessor<Pair<VideoDto, VideoCumulativeStats>, VideoDailyStats> viewsDiffProcessor() {
        return new ItemProcessor<Pair<VideoDto, VideoCumulativeStats>, VideoDailyStats>() {
            @Override
            public VideoDailyStats process(Pair<VideoDto, VideoCumulativeStats> pair) throws Exception {
                VideoDto NDayVideo = pair.getLeft();
                VideoCumulativeStats peviousDayVideo = pair.getRight();
                long diff = NDayVideo.getVideoViews() - peviousDayVideo.getCumulativeViews();
                return VideoDailyStats.builder()
                        .videoId(NDayVideo.getId())
                        .dailyViews(diff)
                        .dailyWatchTime(0)
                        .build();
            }
        };
    }

    @Bean
    @StepScope
    public ItemWriter<VideoDailyStats> viewsDiffWriter() {
        return dailyStatsRepository::saveAll;
    }

    // videoWatchTimeTaskletStep : 재생내역에서 동영상 별로 재생 시간 합산(SUM, GROUP BY)한 후 조회하여 Step ExecutionContext에 저장
    @Bean
    public Step videoWatchTimeTaskletStep() {
        return new StepBuilder(BatchConstants.VIDEO_WATCH_TIME+"TaskletStep", jobRepository)
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
    public Step videoWatchTimeStep() throws ParseException {
        return new StepBuilder(BatchConstants.VIDEO_WATCH_TIME+"Step", jobRepository)
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
            @Value("#{jobParameters["+BatchKeys.CURRENT_DATE+"]}") String currentDate) throws ParseException { // 스케줄링 실행할 때 입력받은 날짜 전달 받음

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
            @Value("#{jobExecutionContext["+BatchKeys.VIDEO_WATCH_TIME_LIST+"]}") List<UserVideoHistoryBatchDto> videoWatchTimeList, // step executionContext를 job executionContext로 승격시켜 전달 받음
            @Value("#{jobParameters["+BatchKeys.CURRENT_DATE+"]}") String currentDate) {

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
