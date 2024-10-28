package org.project.paysystem.service;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.RankVideoInfoDto;
import org.project.paysystem.dto.Top5ResponseDto;
import org.project.paysystem.dto.TopViewsRequestDto;
import org.project.paysystem.repository.VideoDailyStatsRepository;
import org.project.paysystem.repository.VideoMonthlyStatsRepository;
import org.project.paysystem.repository.VideoRepository;
import org.project.paysystem.repository.VideoWeeklyStatsRepository;
import org.project.paysystem.util.LastUpdatedStatsUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoStatsService {

    private final VideoDailyStatsRepository dailyStatsRepository;
    private final VideoWeeklyStatsRepository weeklyStatsRepository;
    private final VideoMonthlyStatsRepository monthlyStatsRepository;
    private final VideoRepository videoRepository;

    private final LastUpdatedStatsUtil lastUpdatedStatsUtil;



    public Top5ResponseDto top5ViewsVideo(TopViewsRequestDto requestDto) {
        String currentDate = requestDto.getCurrentDate() == null ? String.valueOf(LocalDate.now()) : requestDto.getCurrentDate();
        String period = requestDto.getPeriod();

        // 일, 주, 월에 따른 조회수 Top5 가져오기
        List<RankVideoInfoDto> rankVideoInfoDtoList = fetchPeriodData(currentDate, period);

        return Top5ResponseDto.builder()
                .period(period)
                .searchDate(LocalDate.parse(currentDate))
                .rankVideoInfoDtoList(rankVideoInfoDtoList)
                .build();
    }

    // 일, 주, 월에 따른 조회수 Top5 가져오기
    @Transactional
    public List<RankVideoInfoDto> fetchPeriodData(String currentDate, String period) {
        List<RankVideoInfoDto> rankVideoInfoDtoList = new ArrayList<>();
        LocalDate lastUpdateDate = lastUpdatedStatsUtil.fetchLastUpdatedStatsDate(period); // 통계 테이블에 마지막으로 적재한 날짜 가져오기

        if(period.equals("day")) {
            LocalDate targetDate = LocalDate.parse(currentDate.substring(0, 10));

            if(!targetDate.isAfter(lastUpdateDate)) { // 일별 테이블에서 가져오기
                rankVideoInfoDtoList = dailyStatsRepository.findTop5ByCreatedAtOrderByDailyViewsDesc(targetDate);
            }


        } else if(period.equals("week")) {
            LocalDate targetDate = LocalDate.parse(currentDate.substring(0, 10));

            if(!targetDate.isAfter(lastUpdateDate)) { // 주별 테이블에서 가져오기
                LocalDate nextSunday = getNextSundayFromTargetDate(targetDate); // targetDate 이후 가장 가까운 일요일을 계산
                rankVideoInfoDtoList = weeklyStatsRepository.findTop5ByCreatedAtOrderByWeeklyViewsDesc(nextSunday);
            }

        } else if(period.equals("month")) {
            YearMonth lastUpdatedMonth = YearMonth.from(lastUpdateDate);
            YearMonth targetMonth = YearMonth.parse(currentDate.substring(0, 7)); // 'yyyy-mm'

            if(!targetMonth.isAfter(lastUpdatedMonth)) {
                rankVideoInfoDtoList = monthlyStatsRepository.findTop5ByCreatedAtOrderByMonthlyViewsDesc(targetMonth);
            }

        }

        for(int i = 0 ; i < rankVideoInfoDtoList.size(); i++) {
            rankVideoInfoDtoList.get(i).updateRank(i+1);
        }

        return rankVideoInfoDtoList;
    }

    public LocalDate getWeeklyLastUpdate() {
        return weeklyStatsRepository.findWeeklyLastUpdate();
    }

    private LocalDate getNextSundayFromTargetDate(LocalDate targetDate) {
        return targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public LocalDate getMonthlyLastUpdate() {
        return monthlyStatsRepository.findMonthlyLastUpdate();
    }

    public LocalDate getDailyLastUpdate() {
        return dailyStatsRepository.findLastUpdate();
    }

}
