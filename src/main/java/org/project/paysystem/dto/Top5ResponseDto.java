package org.project.paysystem.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Top5ResponseDto {
    private int count;
    private String period;
    private LocalDate searchDate;
    private List<RankVideoInfoDto> rankVideoInfoDtoList;

    @Builder
    public Top5ResponseDto(String period, LocalDate searchDate, List<RankVideoInfoDto> rankVideoInfoDtoList) {
        this.count = rankVideoInfoDtoList.size();
        this.period = period;
        this.searchDate = searchDate;
        this.rankVideoInfoDtoList = rankVideoInfoDtoList;
    }
}
