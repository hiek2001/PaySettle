package com.project.revenueservice.batch;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.VideoDto;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@SpringBootTest
public class DailyStatsBatchTest {

    @Lazy
    @Autowired
    private StreamingServiceClient streamingClient;


    private final String currentDate = "2024-11-06";

    @Test
    public void videoAfterId_API_기능검증() throws Exception {
        List<VideoDto> response = streamingClient.getVideosAfterId(0, 10);
        AssertionsForInterfaceTypes.assertThat(response).isNotEmpty(); // 정상 응답이 있을 경우에 대한 검증
    }

}
