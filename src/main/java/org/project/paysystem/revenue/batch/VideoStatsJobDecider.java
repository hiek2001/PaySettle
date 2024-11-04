package org.project.paysystem.revenue.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

@Component
public class VideoStatsJobDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (jobExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
            return new FlowExecutionStatus("COMPLETED");
        } else {
            return new FlowExecutionStatus("FAILED");
        }
    }

}
