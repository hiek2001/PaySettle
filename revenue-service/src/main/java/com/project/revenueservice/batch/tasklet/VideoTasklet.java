package com.project.revenueservice.batch.tasklet;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class VideoTasklet implements Tasklet, StepExecutionListener {
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        return RepeatStatus.FINISHED;
    }

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        StepExecutionListener.super.beforeStep(stepExecution);
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        return StepExecutionListener.super.afterStep(stepExecution);
    }
}
