package com.nss.pibblest.modules.sales.internal.web.requests.createSale;

public class CreateSaleResponse {
    private String message;

    public CreateSaleResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
