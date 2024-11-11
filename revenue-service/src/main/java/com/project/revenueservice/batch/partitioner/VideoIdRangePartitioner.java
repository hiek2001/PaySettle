package com.project.revenueservice.batch.partitioner;

import com.project.revenueservice.client.StreamingServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class VideoIdRangePartitioner implements Partitioner {

    private final StreamingServiceClient streamingClient;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new HashMap<String, ExecutionContext>();
        int totalPageCount = streamingClient.getVideoTotalCount();
        int pagesPerPartition = totalPageCount / gridSize;

        for(int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.put("startPage", i * pagesPerPartition);
            context.put("endPage", (i+1) * pagesPerPartition - 1);
            partitions.put("partition" + i, context);
        }
        return partitions;
    }

}
