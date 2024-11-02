package com.project.revenueservice.util;

import com.project.revenueservice.client.StatsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LastUpdatedStatsUtil {

   // private final RestTemplate restTemplate;
    private final StatsClient statsClient;

//    @Value("${url.base}")
//    private String baseUrl;

    public LocalDate fetchLastUpdatedStatsDate(String period, String revenueType) {
        String response = statsClient.fetchLastUpdatedStatsDate(period, revenueType);

        if (response != null && !response.isEmpty()) {
            return LocalDate.parse(response.replace("\"", ""));
        } else {
            throw new RuntimeException("Failed to fetch last updated " + period + " stats date.");
        }
    }
}
