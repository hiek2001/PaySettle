package com.project.revenueservice.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtils {

    public boolean isSunday() {
        return LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
