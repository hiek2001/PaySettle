package com.project.revenueservice.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class PeriodDateRange {
    private final LocalDate targetDate;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String targetMonth;

    public PeriodDateRange(String period, LocalDate targetDate) {
        this.targetDate = targetDate;

        switch (period.toLowerCase()) {
            case "day":
                this.startDate = targetDate;
                this.endDate = targetDate;
                this.targetMonth = targetDate.toString().substring(0, 7);

                break;

            case "week":
                this.startDate = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                this.endDate = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                this.targetMonth = targetDate.toString().substring(0, 7);

                break;

            case "month":
                this.startDate = targetDate.with(TemporalAdjusters.firstDayOfMonth());
                this.endDate = targetDate.with(TemporalAdjusters.lastDayOfMonth());
                this.targetMonth = targetDate.toString().substring(0, 7);

                break;

            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getTargetMonth() {
        return targetMonth;
    }
}

