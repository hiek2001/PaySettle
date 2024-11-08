package com.project.revenueservice.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class ChunkExecutionTimeListener implements StepExecutionListener, ItemReadListener<Object>, ItemProcessListener<Object, Object> , ChunkListener {
    private Instant readStartTime;
    private Instant processStartTime;
    private long totalReadTime = 0;
    private long totalProcessTime = 0;
    private long chunkReadTime = 0;
    private long chunkProcessTime = 0;
    private Instant chunkStartTime;
    private Instant chunkEndTime;

    private Instant stepStartTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        totalReadTime = 0;
        totalProcessTime = 0;

        stepStartTime = Instant.now();
        log.info("========= Step execution started =========");
        log.info("Step {} 시작 시간: {}",stepExecution.getStepName(), stepStartTime);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Instant stepEndTime = Instant.now();
        Duration duration = Duration.between(stepStartTime, stepEndTime);

        log.info("Step {} 종료 시간 : {} " ,stepExecution.getStepName(), stepEndTime);
        log.info("========= Step execution completed =========");
        log.info("총 count : {} 개", stepExecution.getReadCount());
        log.info("Reader 총 소요 시간: {} ms", totalReadTime);
        log.info("Processor 총 소요 시간: {} ms", totalProcessTime);
        log.info("Step {} 총 처리 시간 : {} sec",stepExecution.getStepName(),duration.toSeconds());

        return stepExecution.getExitStatus();
    }

    @Override
    public void beforeRead() {
        readStartTime = Instant.now();
    }

    @Override
    public void afterRead(Object item) {
        Instant readEndTime = Instant.now();
        Duration readDuration = Duration.between(readStartTime, readEndTime);
        chunkReadTime += readDuration.toMillis();
    }

    @Override
    public void onReadError(Exception ex) {
        log.error("Reader에서 에러 발생", ex);
    }

    @Override
    public void beforeProcess(Object item) {
        processStartTime = Instant.now();
    }

    @Override
    public void afterProcess(Object item, Object result) {
        Instant processEndTime = Instant.now();
        Duration processDuration = Duration.between(processStartTime, processEndTime);
        chunkProcessTime  += processDuration.toMillis();
    }

    @Override
    public void onProcessError(Object item, Exception ex) {
        log.error("Processor에서 에러 발생", ex);
    }

    @Override
    public void beforeChunk(ChunkContext context) {
        chunkReadTime = 0;
        chunkProcessTime = 0;
        chunkStartTime = Instant.now();
    }

    @Override
    public void afterChunk(ChunkContext context) {
        chunkEndTime = Instant.now();
        Duration chunkDuration = Duration.between(chunkStartTime, chunkEndTime);

        // 각 chunk의 누적 시간을 전체 Reader, Processor 소요 시간에 추가
        totalReadTime += chunkReadTime;
        totalProcessTime += chunkProcessTime;

        log.info("Chunk 소요 시간: {} ms", chunkDuration.toMillis());
        log.info("Chunk 내 Reader 소요 시간: {} ms", chunkReadTime);
        log.info("Chunk 내 Processor 소요 시간: {} ms", chunkProcessTime);
    }

}
