package com.nss.pibblest.modules.sales.internal.web.requests.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequest {
    @NotNull(message = "saleId is required")
    private Long saleId;

    @NotNull(message = "storeId is required")
    private Long storeId;
}