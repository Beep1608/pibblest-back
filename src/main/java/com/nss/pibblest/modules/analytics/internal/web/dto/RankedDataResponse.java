package com.nss.pibblest.modules.analytics.internal.web.dto;

import java.util.List;
import java.util.Map;

public record RankedDataResponse<T>(
    Map<String, Object> filters,
    List<T> data
) {}
