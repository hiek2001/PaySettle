package com.project.revenueservice.batch;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.VideoDto;
import com.project.revenueservice.entity.VideoCumulativeStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class DailyStatsReaderTest {
    @MockBean
    private StreamingServiceClient streamingClient;

    @Autowired
    private ItemReader<VideoDto> viewsReader;
    @Autowired
    private JdbcPagingItemReader<VideoCumulativeStats> getPreviousDayCumulativeReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql(statements = {
            "INSERT INTO video_cumulative_stats (id, video_id, created_at, cumulative_views, cumulative_watch_time) VALUES (10000007, 101, '2024-11-04', 100, 2000)",
            "INSERT INTO video_cumulative_stats (id, video_id, created_at, cumulative_views, cumulative_watch_time) VALUES (10000008, 102, '2024-11-04', 150, 2500)",
            "INSERT INTO video_cumulative_stats (id, video_id, created_at, cumulative_views, cumulative_watch_time) VALUES (10000009, 103, '2024-11-03', 200, 3000)"
    })
    @Rollback
    public void 일별_누적_조회수_Reader_동일_날짜_반환() throws Exception {
        // 테스트 날짜 설정
        String testDate = "2024-11-04";

        // JobParameters 설정
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("currentDate", testDate)
                .toJobParameters();

        // Reader에 jobParameters로 날짜 전달
        getPreviousDayCumulativeReader.open(MetaDataInstanceFactory.createStepExecution(jobParameters).getExecutionContext());
        getPreviousDayCumulativeReader.setPageSize(10); // 페이징 크기 설정

        VideoCumulativeStats item;
        int count = 0;
        while ((item = getPreviousDayCumulativeReader.read()) != null) {
            System.out.println("Read item: " + item);
            count++;
        }

        // Reader에서 반환된 데이터 검증
        assertThat(count).isEqualTo(2); // `2024-11-04` 날짜의 데이터 2개가 반환

        getPreviousDayCumulativeReader.close();
    }


   @Test
    public void viewsReader_정상_테스트() throws Exception {
       List<VideoDto> videoList = Arrays.asList(
               new VideoDto(1L, 560L),
               new VideoDto(2L, 120L)
       );

       when(streamingClient.getAllVideos()).thenReturn(videoList);

       assertEquals(videoList.get(0), viewsReader.read());
       assertEquals(videoList.get(1), viewsReader.read());

       assertNull(viewsReader.read());
   }
}
