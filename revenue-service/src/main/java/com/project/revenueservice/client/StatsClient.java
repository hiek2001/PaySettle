package com.project.revenueservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "statsClient", url = "http://localhost:9004")
public interface StatsClient {

    @GetMapping("/api/revenue/stats/{period}/last-updated")
    String fetchLastUpdatedStatsDate(
            @PathVariable("period") String period,
            @RequestParam(value = "revenueType", required = false) String revenueType
    );

}
