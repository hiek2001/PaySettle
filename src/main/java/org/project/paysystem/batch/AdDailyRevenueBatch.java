package org.project.paysystem.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.paysystem.dto.AdCountBatchDto;
import org.project.paysystem.entity.AdDailyRevenue;
import org.project.paysystem.entity.GlobalPricing;
import org.project.paysystem.entity.MediaTypeEnum;
import org.project.paysystem.entity.Video;
import org.project.paysystem.repository.AdDailyRevenueRepository;
import org.project.paysystem.repository.GlobalPricingRepository;
import org.project.paysystem.repository.VideoRepository;
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
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "광고 일별 정산 배치")
@Configuration
@RequiredArgsConstructor
public class AdDailyRevenueBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final GlobalPricingRepository pricingRepository;
    private final VideoRepository videoRepository;
    private final AdDailyRevenueRepository adRevenueRepository;

    private final int chunkSize = 10;
    private MediaTypeEnum type = MediaTypeEnum.AD;

    @Bean
    public Job adDailyRevenueJob() throws ParseException {
        log.info("adDailyRevenueJob");

        return new JobBuilder("adDailyRevenueJob", jobRepository)
                .start(globalPricingTaskletStep())
                .next(adDailyRevenueStep())
                .build();
    }

    // tasklet
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

    // Ad daily revenue step
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
    public JpaPagingItemReader<AdCountBatchDto> getAdDailyRevenueReader(
            @Value("#{jobParameters[currentDate]}") String currentDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(currentDate.substring(0, 10), formatter);

        return new JpaPagingItemReaderBuilder<AdCountBatchDto>()
                .name("getAdDailyRevenueReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT new org.project.paysystem.dto.AdCountBatchDto(vah.video.id, COUNT(vah.ad.id)) FROM VideoAdHistory vah "+
                            "WHERE vah.createdAt = :currentDate GROUP BY vah.video.id"
                )
                .parameterValues(Map.of("currentDate", parsedDate))
                .pageSize(chunkSize)
                .build();

    }

    @Bean
    @StepScope
    public ItemProcessor<AdCountBatchDto, AdDailyRevenue> adDailyRevenueProcessor(
            @Value("#{jobExecutionContext[priceList]}") List<GlobalPricing> priceList) {

        return new ItemProcessor<AdCountBatchDto, AdDailyRevenue>() {

            @Override
            public AdDailyRevenue process(AdCountBatchDto item) throws Exception {
                long totalAmount = calculateAmountForViews(priceList, item.getAdCount());

                Video video = videoRepository.batchFindById(item.getVideoId());
                return AdDailyRevenue.builder()
                        .video(video)
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

    public long calculateAmountForViews(List<GlobalPricing> priceList, long views) {
        long totalAmount = 0;
        long remainingViews = views;

        for(GlobalPricing price : priceList) {
            // 현재 구간의 조회수 범위
            long minViews = price.getMinViews();
            long maxViews = (price.getMaxViews() == 0) ? remainingViews : price.getMaxViews();

            // 남은 조회수가 현재 구간의 조회수 범위를 초과할 경우
            if(remainingViews > minViews) {
                long viewsInRange =  maxViews - minViews;

                totalAmount += (long) (viewsInRange * price.getUnitPrice());
                remainingViews -= viewsInRange;

                if(remainingViews <= 0) { // 모든 조회수 처리 완료
                    break;
                }
            }
        }
        return totalAmount;
    }
}
