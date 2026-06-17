package com.nss.pibblest.modules.analytics.internal.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class DateRangeResolver {
    public record RangePair(LocalDateTime start, LocalDateTime end) {}

    public RangePair resolve(String range, LocalDate start, LocalDate end) {
        LocalDateTime now = LocalDateTime.now();
        return switch (range) {
            case "today" -> new RangePair(now.with(LocalTime.MIN), now.with(LocalTime.MAX));
            case "last7days" -> new RangePair(now.minusDays(7).with(LocalTime.MIN), now.with(LocalTime.MAX));
            case "last30days" -> new RangePair(now.minusDays(30).with(LocalTime.MIN), now.with(LocalTime.MAX));
            case "month" -> new RangePair(now.withDayOfMonth(1).with(LocalTime.MIN), now.with(LocalTime.MAX));
            case "custom" -> new RangePair(start.atStartOfDay(), end.atTime(LocalTime.MAX));
            default -> throw new IllegalArgumentException("Unknown range: " + range);
        };
    }
}
