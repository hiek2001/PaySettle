package org.project.paysystem.dto;

import lombok.Getter;


@Getter
public class Top5RequestDto {
    private String period; // day, week, month (일, 주, 월)
    private String type; // views, watchtime (조회수, 재생 시간)
    private String currentDate; // 확인할 날짜, 값이 없다면 현재 날짜로 지정
}
