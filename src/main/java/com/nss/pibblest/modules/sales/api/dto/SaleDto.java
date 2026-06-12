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
    String employeeUsername, // ✨ NUEVO: Nombre de usuario del creador de la venta
    List<SaleDetailDto> details
) {}
