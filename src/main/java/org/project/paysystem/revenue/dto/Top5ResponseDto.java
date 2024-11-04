package org.project.paysystem.revenue.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Top5ResponseDto {
    private int count;
    private String period;
    private String type;
    private LocalDate searchDate;
    private List<RankVideoInfoDto> rankVideoInfoDtoList;

    @Builder
    public Top5ResponseDto(String period, String type, LocalDate searchDate, List<RankVideoInfoDto> rankVideoInfoDtoList) {
        this.count = rankVideoInfoDtoList.size();
        this.type = type;
        this.period = period;
        this.searchDate = searchDate;
        this.rankVideoInfoDtoList = rankVideoInfoDtoList;
    }
}
