package com.project.revenueservice.batch;

import com.project.revenueservice.batch.partitioner.VideoIdRangePartitioner;
import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.entity.VideoDailyStats;
import com.project.revenueservice.repository.VideoDailyStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@SpringBatchTest
public class VideoIdRangePartitionerTest {
    public static final DateTimeFormatter FORMATTER = ofPattern("yyyy-MM-dd");

    @InjectMocks
    private static VideoIdRangePartitioner partitioner;

    @Mock
    private StreamingServiceClient streamingClient;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockBean
    private VideoDailyStatsRepository videoDailyStatsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private static Job videoDailyStatsJob;


    @Test
    void 일별_통계를_저장한다() throws Exception {
        LocalDate currentDate = LocalDate.of(2024,11,8);

        int expectedCount = 500000;

        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("currentDate", currentDate.format(FORMATTER))
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<VideoDailyStats> dailyStats = videoDailyStatsRepository.findAll();
        assertThat(dailyStats.size()).isEqualTo(expectedCount);

        List<Map<String, Object>> metaTable = jdbcTemplate.queryForList("select step_name, status, commit_count, read_count, write_count from BATCH_STEP_EXECUTION");

        for (Map<String, Object> step : metaTable) {
            log.info("meta table row={}", step);
        }
    }

    @Test
    void videoTotalCount를_가져온다() throws Exception {
        int response = streamingClient.getVideoTotalCount();
        assertThat(response).isNotEqualTo(0);
    }

    @Test
    void gridSize에_맞게_videoId가_분할된다() throws Exception {
        // given
        int totalCount = 100;
        int gridSize = 5;
        when(streamingClient.getVideoTotalCount()).thenReturn(totalCount);

        partitioner = new VideoIdRangePartitioner(streamingClient);

        // when
        Map<String, ExecutionContext> partitions = partitioner.partition(gridSize);

        // 각 파티션의 startPage와 endPage 검증
        int pagesPerPartition = totalCount / gridSize;
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = partitions.get("partition" + i);
            int expectedStartPage = i * pagesPerPartition;
            int expectedEndPage = (i + 1) * pagesPerPartition - 1;

            assertEquals(expectedStartPage, context.getInt("startPage"));
            assertEquals(expectedEndPage, context.getInt("endPage"));
        }
    }
}
