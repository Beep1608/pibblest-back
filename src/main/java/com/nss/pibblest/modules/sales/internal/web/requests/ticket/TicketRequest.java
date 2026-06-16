package com.nss.pibblest.modules.sales.internal.web.requests.ticket;

import jakarta.validation.constraints.NotNull;

public class TicketRequest {
    @NotNull(message = "saleId is required")
    private Long saleId;

    @NotNull(message = "storeId is required")
    private Long storeId;

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
