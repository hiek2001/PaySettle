package org.project.paysystem.revenue.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class RevenueResponseDto {
    private int count;
    private String period;
    private String revenueType;
    private LocalDate searchDate;
    private List<RevenueInfoDto> revenueList;

    @Builder
    public RevenueResponseDto(String period, String revenueType, LocalDate searchDate, List<RevenueInfoDto> revenueList) {
        this.count = revenueList.size();
        this.period = period;
        this.revenueType = revenueType;
        this.searchDate = searchDate;
        this.revenueList = revenueList;
    }
}
