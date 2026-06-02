package com.nss.pibblest.modules.sales.api.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record SaleDto(
    Long id,
    Long storeId,
    BigDecimal totalAmount,
    String status,
    ZonedDateTime createdAt,
    List<SaleDetailDto> details
) {}
