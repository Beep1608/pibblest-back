package com.nss.pibblest.modules.analytics.internal.web.dto;

import java.time.LocalDate;

public record AnalyticsFilterRequest(
    String range,
    LocalDate startDate,
    LocalDate endDate,
    String granularity,
    Long storeId
) {}
