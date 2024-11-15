package com.project.revenueservice.service;

import com.project.revenueservice.dto.RevenueInfoDto;
import com.project.revenueservice.dto.RevenueInfoListWrapper;
import com.project.revenueservice.dto.RevenueRequestDto;
import com.project.revenueservice.dto.RevenueResponseDto;
import com.project.revenueservice.repository.AdDailyRevenueRepository;
import com.project.revenueservice.repository.VideoDailyRevenueRepository;
import com.project.revenueservice.util.PeriodDateRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueService {

    private final VideoDailyRevenueRepository videoRevenueRepository;
    private final AdDailyRevenueRepository adRevenueRepository;

    private final RedisTemplate redisTemplate;

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
        
        if(revenueType.equals("video")) {
            return fetchVideoRevenue(period, dateRange);
        } else if(revenueType.equals("ad")) {
            return fetchAdRevenue(period, dateRange);
        }
        return Collections.emptyList();
    }

    // 동영상 정산 데이터 가져오기
    private List<RevenueInfoDto> fetchVideoRevenue(String period, PeriodDateRange dateRange) {
        String cacheKey = generateCacheKey(period, "video", dateRange);

        RevenueInfoListWrapper cachedData = (RevenueInfoListWrapper) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData.getRevenues();
        }

        List<RevenueInfoDto> resultList;
        if (period.equals("day")) {
            resultList = videoRevenueRepository.findRevenueByDay(dateRange.getTargetDate());
        } else if (period.equals("week")) {
            resultList = videoRevenueRepository.findRevenueByWeek(dateRange.getStartDate(), dateRange.getEndDate());
        } else {
            resultList = videoRevenueRepository.findRevenueByMonth(dateRange.getTargetMonth());
        }

        RevenueInfoListWrapper wrapper = new RevenueInfoListWrapper(resultList);
        Duration ttl = determineTTL(period);
        redisTemplate.opsForValue().set(cacheKey, wrapper, ttl);
        return resultList;
    }

    // 광고 정산 데이터 가져오기
    private List<RevenueInfoDto> fetchAdRevenue(String period, PeriodDateRange dateRange) {
        String cacheKey = generateCacheKey(period, "ad", dateRange);

        RevenueInfoListWrapper cachedData = (RevenueInfoListWrapper) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData.getRevenues();
        }

        List<RevenueInfoDto> resultList;
        if (period.equals("day")) {
            resultList = adRevenueRepository.findRevenueByDay(dateRange.getTargetDate());
        } else if (period.equals("week")) {
            resultList = adRevenueRepository.findRevenueByWeek(dateRange.getStartDate(), dateRange.getEndDate());
        } else {
            resultList = adRevenueRepository.findRevenueByMonth(dateRange.getTargetMonth());
        }

        Duration ttl = determineTTL(period);
        redisTemplate.opsForValue().set(cacheKey, resultList, ttl);
        return resultList;
    }

    // TTL 주기 설정
    private Duration determineTTL(String period) {
        switch (period) {
            case "day" : return Duration.ofDays(1); // 일별 데이터는 1일
            case "week" : return Duration.ofDays(7); // 주별 데이터는 7일
            case "month" : return Duration.ofDays(30); // 월별 데이터는 30일
            default: throw new IllegalArgumentException("지원하지 않는 주기입니다: " + period);
        }
    }

    // 정산 금액 계산
    private Map<Long, RevenueInfoDto> calculateTotalRevenue(List<RevenueInfoDto> videoRevenueList, List<RevenueInfoDto> adRevenueList) {
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

    private String generateCacheKey(String period, String revenueType, PeriodDateRange dateRange) {
        // 날짜 정보를 문자열로 변환하여 캐시 키에 포함
        String datePart = period.equals("day") ? dateRange.getTargetDate().toString() :
                period.equals("week") ? dateRange.getStartDate() + "_" + dateRange.getEndDate() :
                        dateRange.getTargetMonth();

        // 캐시 키 생성
        return "revenue:" + revenueType + ":" + period + ":" + datePart;
    }
    // todo : 만약 정산 데이터 계산이 잘못 되어 수정하는 경우, 해당 로직 추가할 때 캐시 무효화 추가 예정
}
