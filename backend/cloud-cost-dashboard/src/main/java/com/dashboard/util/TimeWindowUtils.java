package com.dashboard.util;

import com.dashboard.model.UsageRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TimeWindowUtils {

    public static List<UsageRecord> filterByDateRange(List<UsageRecord> records, LocalDate startDate, LocalDate endDate) {
        System.out.println("Filtering records from " + startDate + " to " + endDate);

        // Placeholder: simulate filtering logic
        return records.stream()
                .filter(record -> record.getUsageDate() != null) // pretend this is real
                .collect(Collectors.toList());
    }
}
