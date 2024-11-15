package com.project.revenueservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RevenueInfoListWrapper implements Serializable {
    private List<RevenueInfoDto> revenues = new ArrayList<>();

    public RevenueInfoListWrapper(List<RevenueInfoDto> revenueInfoDtoList) {
        this.revenues = revenueInfoDtoList;
    }

    public void addList(RevenueInfoDto revenueInfoDto) {
        this.revenues.add(revenueInfoDto);
    }

    public List<RevenueInfoDto> getRevenues() {
        return revenues;
    }
}
