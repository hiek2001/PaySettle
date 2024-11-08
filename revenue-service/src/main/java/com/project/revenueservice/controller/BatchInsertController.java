package com.project.revenueservice.controller;

import com.project.revenueservice.service.VideoBatchInsertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchInsertController {
    private final VideoBatchInsertService videoBatchInsertService;

    @PostMapping("/batch/insert/video")
    public void InsertVideoData() {
        videoBatchInsertService.insertDummyData(1100956, 48899044);
    }

    @PostMapping("/batch/insert/cumulate-video")
    public void InsertCumulateVideoData() {
        videoBatchInsertService.insertDummyCumulateData(1914108, 28085893);
    }

}
