package org.project.paysystem.controller;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.Top5ResponseDto;
import org.project.paysystem.dto.TopViewsRequestDto;
import org.project.paysystem.service.VideoStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class VideoStatsController {

    private final VideoStatsService videoStatsService;

    // 일, 주, 월별 조회수 Top5 조회
    @GetMapping("/top5-views-video")
    public Top5ResponseDto top5ViewsVideo(@RequestBody TopViewsRequestDto requestDto) {
        return videoStatsService.top5ViewsVideo(requestDto);
    }

    // 일별 테이블의 최신 적재 날짜 가져오기
    @GetMapping("/day/last-updated")
    public LocalDate dailyLastUpdated() {
        return videoStatsService.getDailyLastUpdate();
    }

    // 주간 테이블의 최신 적재 날짜 가져오기
    @GetMapping("/week/last-updated")
    public LocalDate weeklyLastUpdated() {
        return videoStatsService.getWeeklyLastUpdate();
    }

    // 월별 테이블의 최신 적재 날짜 가져오기
    @GetMapping("/month/last-updated")
    public LocalDate monthlyLastUpdated() {
        return videoStatsService.getMonthlyLastUpdate();
    }
}
