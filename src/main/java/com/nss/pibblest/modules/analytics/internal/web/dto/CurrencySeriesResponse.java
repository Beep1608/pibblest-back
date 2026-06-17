package com.nss.pibblest.modules.analytics.internal.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CurrencySeriesResponse(
    Map<String, Object> filters,
    Map<String, List<BucketEntry>> series
) {}

record BucketEntry(String bucket, BigDecimal value) {}
