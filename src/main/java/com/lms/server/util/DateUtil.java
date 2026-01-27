package com.lms.server.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtil {

    public static int calculateBusinessDays(LocalDate start, LocalDate end) {
        int days = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }
}