package com.project.revenueservice.controller;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class JobControlController {
    @Autowired
    private JobOperator jobOperator;

    // 실행 중인 Job을 중지하는 API
    @PostMapping("/batch/stop")
    public String stopRunningJob(@RequestParam String jobName) {
        try {
            // 실행 중인 JobExecution ID 조회
            Set<Long> runningExecutions = jobOperator.getRunningExecutions(jobName);
            if (runningExecutions.isEmpty()) {
                return "현재 실행 중인 Job이 없습니다: " + jobName;
            } else {
                // 실행 중인 Job을 중지
                for (Long executionId : runningExecutions) {
                    jobOperator.stop(executionId);
                    System.out.println("실행 중인 Job 중지됨. JobExecution ID: " + executionId);
                }
                return "실행 중인 Job이 중지되었습니다. Job 이름: " + jobName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Job 중지 중 오류 발생: " + e.getMessage();
        }
    }
}
