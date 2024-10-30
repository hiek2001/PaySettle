package org.project.paysystem.controller;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.RevenueRequestDto;
import org.project.paysystem.dto.RevenueResponseDto;
import org.project.paysystem.service.RevenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/revenue")
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping("/details")
    public RevenueResponseDto getRevenue(@RequestBody RevenueRequestDto requestDto) {
        return revenueService.getRevenue(requestDto);
    }

    // 동영상 일별 정산 테이블의 최신 적재 날짜 가져오기
    @GetMapping("/day/video/last-updated")
    public LocalDate getLastUpdatedVideoRevenue() {
        return revenueService.getDailyVideoLastUpdate();
    }

    // 광고 일별 정산 테이블의 최신 적재 날짜 가져오기
    @GetMapping("/day/ad/last-updated")
    public LocalDate getLastUpdatedAdRevenue() {
        return revenueService.getDailyAdLastUpdate();
    }
}
