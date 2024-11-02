package com.project.streamingservice.controller;

import com.project.streamingservice.dto.AdCountBatchDto;
import com.project.streamingservice.service.StreamingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "광고 배치 API")
@RequestMapping("/api/streaming/ad-batch")
public class AdBatchController {

    private final StreamingService streamingService;

    // batch
    @GetMapping("/count-by-date")
    public List<AdCountBatchDto> getAdCountByDate(@RequestParam("currentDate") LocalDate currentDate) {
        return streamingService.getAdCountByDate(currentDate);
    }
}
