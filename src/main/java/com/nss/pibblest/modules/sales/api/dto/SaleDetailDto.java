package com.nss.pibblest.modules.sales.api.dto;

import java.math.BigDecimal;

public record SaleDetailDto(
    Long id,
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
