package org.project.paysystem.service;

import lombok.RequiredArgsConstructor;
import org.project.paysystem.dto.RevenueInfoDto;
import org.project.paysystem.dto.RevenueRequestDto;
import org.project.paysystem.dto.RevenueResponseDto;
import org.project.paysystem.repository.AdDailyRevenueRepository;
import org.project.paysystem.repository.VideoDailyRevenueRepository;
import org.project.paysystem.util.LastUpdatedStatsUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final VideoDailyRevenueRepository videoRevenueRepository;
    private final AdDailyRevenueRepository adRevenueRepository;

    private final LastUpdatedStatsUtil lastUpdatedStatsUtil;

    @Transactional
    public RevenueResponseDto getRevenue(RevenueRequestDto requestDto) {
        String currentDate = requestDto.getCurrentDate() == null ? String.valueOf(LocalDate.now()) : requestDto.getCurrentDate();
        String period = requestDto.getPeriod();
        String revenueType = requestDto.getRevenueType();

        List<RevenueInfoDto> revenueList = fetchPeriodRevenue(currentDate, period, revenueType);

        return RevenueResponseDto.builder()
                .revenueType(revenueType)
                .period(period)
                .searchDate(LocalDate.parse(currentDate))
                .revenueList(revenueList)
                .build();
    }


    public List<RevenueInfoDto> fetchPeriodRevenue(String currentDate, String period, String revenueType) {
        List<RevenueInfoDto> revenueList = new ArrayList<>();
        LocalDate targetDate = LocalDate.parse(currentDate.substring(0, 10));

            if(revenueType.equals("total")) {
                List<RevenueInfoDto> videoRevenueList = fetchRevenueByType(period, "video", targetDate);
                List<RevenueInfoDto> adRevenueList = fetchRevenueByType(period, "ad", targetDate);

                Map<Long, RevenueInfoDto> revenueMap = calculateTotalRevenue(videoRevenueList, adRevenueList);
                revenueList = new ArrayList<>(revenueMap.values());

            } else { // video 또는 ad 일 경우
                revenueList = fetchRevenueByType(period, revenueType, targetDate);
            }

        return revenueList;
    }



    public List<RevenueInfoDto> fetchRevenueByType(String period, String revenueType, LocalDate targetDate) {
        List<RevenueInfoDto> resultList = new ArrayList<>();
        LocalDate startDate = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        String targetMonth = targetDate.toString().substring(0, 7);

        if (revenueType.equals("video")) {
            if(period.equals("day")) {
                resultList =  videoRevenueRepository.findRevenueByDay(targetDate);
            } else if(period.equals("week")) {
                resultList =  videoRevenueRepository.findRevenueByWeek(startDate, endDate);
            } else if(period.equals("month")) {
                resultList =  videoRevenueRepository.findRevenueByMonth(targetMonth);
            }
        } else if (revenueType.equals("ad")) {
            if(period.equals("day")) {
                resultList =  adRevenueRepository.findRevenueByDay(targetDate);
            } else if(period.equals("week")) {
                resultList =  adRevenueRepository.findRevenueByWeek(startDate, endDate);
            } else if(period.equals("month")) {
                resultList =  adRevenueRepository.findRevenueByMonth(targetMonth);
            }

        }
        return resultList;
    }

    public Map<Long, RevenueInfoDto> calculateTotalRevenue(List<RevenueInfoDto> videoRevenueList, List<RevenueInfoDto> adRevenueList) {
        // videoId를 기준으로 amount를 합산할 Map 생성
        Map<Long, RevenueInfoDto> revenueMap = new HashMap<>();

        // videoRevenueList 처리
        for (RevenueInfoDto videoRevenue : videoRevenueList) {
            Long videoId = videoRevenue.getVideoId();
            revenueMap.put(videoId, new RevenueInfoDto(videoId, videoRevenue.getAmount()));
        }

        // adRevenueList 처리 (videoId가 이미 있는 경우 amount 합산)
        for (RevenueInfoDto adRevenue : adRevenueList) {
            Long videoId = adRevenue.getVideoId();
            if (revenueMap.containsKey(videoId)) {
                // 기존 amount에 새로운 amount를 합산
                RevenueInfoDto existingRevenue = revenueMap.get(videoId);
                existingRevenue.updateAmount(existingRevenue.getAmount() ,adRevenue.getAmount());
            } else {
                // 새로운 videoId인 경우 추가
                revenueMap.put(videoId, new RevenueInfoDto(videoId, adRevenue.getAmount()));
            }
        }
        return revenueMap;
    }


    public LocalDate getDailyVideoLastUpdate() {
        return videoRevenueRepository.findLastUpdate();
    }

    public LocalDate getDailyAdLastUpdate() {
        return adRevenueRepository.findLastUpdate();
    }
}
