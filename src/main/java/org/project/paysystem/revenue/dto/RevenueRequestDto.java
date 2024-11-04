package org.project.paysystem.revenue.dto;

import lombok.Getter;

@Getter
public class RevenueRequestDto {
    private String period; // day, week, month (일, 주, 월)
    private String revenueType; // total(video+ad), video, ad
    private String currentDate; // 확인할 날짜, 값이 없다면 현재 날짜로 지정
}
