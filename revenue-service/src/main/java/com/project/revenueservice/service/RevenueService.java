package com.project.revenueservice.service;

import com.project.revenueservice.dto.RevenueInfoDto;
import com.project.revenueservice.dto.RevenueRequestDto;
import com.project.revenueservice.dto.RevenueResponseDto;
import com.project.revenueservice.repository.AdDailyRevenueRepository;
import com.project.revenueservice.repository.VideoDailyRevenueRepository;
import com.project.revenueservice.util.PeriodDateRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class RevenueService {

    private final VideoDailyRevenueRepository videoRevenueRepository;
    private final AdDailyRevenueRepository adRevenueRepository;

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

    @Transactional
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
        PeriodDateRange dateRange = new PeriodDateRange(period, targetDate);
        List<RevenueInfoDto> resultList = new ArrayList<>();

        if (revenueType.equals("video")) {
            if(period.equals("day")) {
                resultList =  videoRevenueRepository.findRevenueByDay(dateRange.getTargetDate());
            } else if(period.equals("week")) {
                resultList =  videoRevenueRepository.findRevenueByWeek(dateRange.getStartDate(), dateRange.getEndDate());
            } else if(period.equals("month")) {
                resultList =  videoRevenueRepository.findRevenueByMonth(dateRange.getTargetMonth());
            }
        } else if (revenueType.equals("ad")) {
            if(period.equals("day")) {
                resultList =  adRevenueRepository.findRevenueByDay(dateRange.getTargetDate());
            } else if(period.equals("week")) {
                resultList =  adRevenueRepository.findRevenueByWeek(dateRange.getStartDate(), dateRange.getEndDate());
            } else if(period.equals("month")) {
                resultList =  adRevenueRepository.findRevenueByMonth(dateRange.getTargetMonth());
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
