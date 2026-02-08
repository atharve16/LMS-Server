package com.lms.server.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    /**
     * Calculate business days between two dates (inclusive of both start and end date)
     * Excludes weekends (Saturday and Sunday)
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return number of business days
     */
    public static int calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        int businessDays = 0;
        LocalDate current = startDate;

        // Iterate through each day including the end date
        while (!current.isAfter(endDate)) {
            // Check if current day is not Saturday or Sunday
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                businessDays++;
            }
            current = current.plusDays(1);
        }

        return businessDays;
    }

    /**
     * Alternative simpler version that counts all days (including weekends)
     * Use this if your leave system doesn't exclude weekends
     */
    public static int calculateTotalDays(LocalDate startDate, LocalDate endDate) {
        // Add 1 to include both start and end dates
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}