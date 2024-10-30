package org.project.paysystem.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.paysystem.entity.GlobalPricing;
import org.project.paysystem.entity.MediaTypeEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RevenueCalculatorTest {

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

    @Test
    public void 광고_정산_테스트_마지막_구간() {
        // 마지막 구간 테스트
        List<GlobalPricing> priceList = List.of(
                new GlobalPricing(10,MediaTypeEnum.AD, 0, 99999),
                new GlobalPricing(12,MediaTypeEnum.AD, 100000, 499999),
                new GlobalPricing(15, MediaTypeEnum.AD, 500000, 999999),
                new GlobalPricing(20, MediaTypeEnum.AD, 1000000, 0)
        );

        long views = 1200000;

        long expectedAmount = (99999 * 10) + (399999 * 12) + (499999 * 15) + (200003 * 20);
        long actualAmount = calculateAmountForViews(priceList, views);

        assertEquals(expectedAmount, actualAmount);
    }

    @Test
    public void 광고_정산_테스트_여러_구간() {
        // 여러 구간 테스트
        List<GlobalPricing> priceList = List.of(
                new GlobalPricing(10,MediaTypeEnum.AD, 0, 99999),
                new GlobalPricing(12,MediaTypeEnum.AD, 100000, 499999),
                new GlobalPricing(15, MediaTypeEnum.AD, 500000, 999999)
        );

        long views = 600000;

        long expectedAmount = (99999 * 10) + (399999 * 12) + (100002 * 15);
        long actualAmount = calculateAmountForViews(priceList, views);

        assertEquals(expectedAmount, actualAmount);
    }

    @Test
    public void 광고_정산_테스트_단일_구간() {
        // 단일 구간 테스트: 0~99999 조회수, 단가 10원
        List<GlobalPricing> priceList = List.of(new GlobalPricing(10,MediaTypeEnum.AD, 0, 99999));
        long views = 5000;

        long expectedAmount = views * 10;
        long actualAmount = calculateAmountForViews(priceList, views);

        assertEquals(expectedAmount, actualAmount);
    }
}
