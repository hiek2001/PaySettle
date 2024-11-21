package com.project.revenueservice.util;

import com.project.revenueservice.client.StatsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LastUpdatedStatsUtil {

    private final StatsClient statsClient;

    public LocalDate fetchLastUpdatedStatsDate(String period, String revenueType) {
        String response = statsClient.fetchLastUpdatedStatsDate(period, revenueType);

        if (response != null && !response.isEmpty()) {
            return LocalDate.parse(response.replace("\"", ""));
        } else {
            throw new RuntimeException("Failed to fetch last updated " + period + " stats date.");
        }
    }
}
