package org.project.paysystem.dto;

import lombok.Getter;


@Getter
public class TopViewsRequestDto {
    private String period; // day, week, month (일, 주, 월)
    private String currentDate; // 확인할 날짜, 값이 없다면 현재 날짜로 지정
}
