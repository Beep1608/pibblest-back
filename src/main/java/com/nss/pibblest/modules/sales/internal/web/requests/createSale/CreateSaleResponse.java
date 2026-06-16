package com.nss.pibblest.modules.sales.internal.web.requests.createSale;

public class CreateSaleResponse {
    private String message;
    private Long saleId;

    public CreateSaleResponse(String message) {
        this.message = message;
    }

    public CreateSaleResponse(String message, Long saleId) {
        this.message = message;
        this.saleId = saleId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }
}
