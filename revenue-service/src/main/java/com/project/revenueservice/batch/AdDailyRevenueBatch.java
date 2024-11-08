package com.project.revenueservice.batch;

import com.project.revenueservice.client.StreamingServiceClient;
import com.project.revenueservice.dto.AdCountBatchDto;
import com.project.revenueservice.entity.AdDailyRevenue;
import com.project.revenueservice.entity.GlobalPricing;
import com.project.revenueservice.entity.MediaTypeEnum;
import com.project.revenueservice.repository.AdDailyRevenueRepository;
import com.project.revenueservice.repository.GlobalPricingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

@Slf4j(topic = "광고 일별 정산 배치")
@Configuration
@RequiredArgsConstructor
public class AdDailyRevenueBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final GlobalPricingRepository pricingRepository;
    private final AdDailyRevenueRepository adRevenueRepository;

    private final StreamingServiceClient streamingClient;

    @Value("${spring.batch.chunksize}")
    private int chunkSize;

    private MediaTypeEnum type = MediaTypeEnum.AD;

    @Bean
    public Job adDailyRevenueJob() throws ParseException {
        log.info("일별 광고 정산 배치 시작");

        return new JobBuilder("adDailyRevenueJob", jobRepository)
                .start(globalPricingTaskletStep())
                .next(adDailyRevenueStep())
                .build();
    }

    // globalPricingTaskletStep : 광고 조회수별 단가 금액표 db에서 조회하여 Step ExecutionContext에 저장
    @Bean
    public Step globalPricingTaskletStep() {
        return new StepBuilder("globalPricingTaskletStep", jobRepository)
                .tasklet(globalPricingTasklet(), transactionManager)
                .listener(priceListener())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener priceListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"priceList"});
        return listener;
    }

    @Bean
    public Tasklet globalPricingTasklet() {
        return (contribution, chunkContext) -> {
            List<GlobalPricing> priceList = pricingRepository.findAllByType(type);
            ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
            stepContext.put("priceList", priceList);
            return RepeatStatus.FINISHED;
        };
    }

    // adDailyRevenueStep : 동영상 별로 삽입된 광고 갯수(=조회수)를 기반으로 정산 금액 계산하여 저장
    @Bean
    public Step adDailyRevenueStep() throws ParseException {

        return new StepBuilder("adDailyRevenueStep", jobRepository)
                .<AdCountBatchDto, AdDailyRevenue> chunk(chunkSize, transactionManager)
                .reader(getAdDailyRevenueReader(null))
                .processor(adDailyRevenueProcessor(null))
                .writer(adDailyRevenueWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<AdCountBatchDto> getAdDailyRevenueReader(
            @Value("#{jobParameters[currentDate]}") String currentDate) {

        return new ItemReader<>() {
            private Iterator<AdCountBatchDto> iterator;

            @Override
            public AdCountBatchDto read() throws Exception {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(currentDate.substring(0,10), formatter);

                if(iterator == null) {
                    List<AdCountBatchDto> ads = streamingClient.getAdCountByDate(parsedDate); // 스트리밍 서비스의 광고 목록에서 특정 날짜에 해당하는 데이터 조회
                    iterator = ads.iterator();
                }

                return iterator != null && iterator.hasNext() ? iterator.next() : null;
            }
        };

    }

    @Bean
    @StepScope
    public ItemProcessor<AdCountBatchDto, AdDailyRevenue> adDailyRevenueProcessor(
            @Value("#{jobExecutionContext[priceList]}") List<GlobalPricing> priceList) {

        return new ItemProcessor<AdCountBatchDto, AdDailyRevenue>() {

            @Override
            public AdDailyRevenue process(AdCountBatchDto item) throws Exception {
                long totalAmount = calculateAmountForViews(priceList, item.getAdCount()); // 정산 금액 계산

                return AdDailyRevenue.builder()
                        .videoId(item.getVideoId())
                        .adAmount(totalAmount)
                        .build();
            }
        };
    }

    @Bean
    public RepositoryItemWriter<AdDailyRevenue> adDailyRevenueWriter() {
        return new RepositoryItemWriterBuilder<AdDailyRevenue>()
                .repository(adRevenueRepository)
                .methodName("save")
                .build();
    }

    // 정산 금액 계산
    public long calculateAmountForViews(List<GlobalPricing> priceList, long views) {
        long totalAmount = 0;
        long remainingViews = views;

        for (int i = 0; i < priceList.size(); i++) {
            GlobalPricing price = priceList.get(i);
            long minViews = price.getMinViews();
            long maxViews = price.getMaxViews() == 0 ? Long.MAX_VALUE : price.getMaxViews(); // 상한이 없는 경우를 처리

            // 마지막 구간 여부를 확인
            boolean isLastTier = (i == priceList.size() - 1);

            if (isLastTier) {
                // 마지막 구간에서는 remainingViews 전체에 단가를 적용
                totalAmount += (long) (remainingViews * price.getUnitPrice());
                break;
            } else {
                // 마지막 구간이 아닐 때는 구간별 조회수를 계산
                if (remainingViews > minViews) {
                    long viewsInThisRange = Math.min(remainingViews, maxViews - minViews);

                    totalAmount += (long) (viewsInThisRange * price.getUnitPrice());
                    remainingViews -= viewsInThisRange;
                }
            }
            // 남은 조회수가 0이면 더 이상 계산하지 않음
            if (remainingViews <= 0) {
                break;
            }
        }

        return totalAmount;
    }

}
