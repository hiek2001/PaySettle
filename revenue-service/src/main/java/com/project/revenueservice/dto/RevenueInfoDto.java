package com.project.revenueservice.dto;

import lombok.Getter;

@Getter
public class RevenueInfoDto {
    private Long videoId;
    private long amount;

    public RevenueInfoDto(Long videoId, long amount) {
        this.videoId = videoId;
        this.amount = amount;
    }

    public void updateAmount(long videoAmount, long adAmount) {
        this.amount = videoAmount + adAmount;
    }
}
