package com.nss.pibblest.modules.analytics.internal.web.dto;

import java.math.BigDecimal;

public record StoreRankEntry(
    Long storeId,
    String storeName,
    BigDecimal totalRevenue,
    String currencyCode
) {}
