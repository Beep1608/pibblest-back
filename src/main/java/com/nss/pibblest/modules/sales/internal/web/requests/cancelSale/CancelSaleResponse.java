package com.nss.pibblest.modules.sales.internal.web.requests.cancelSale;

public class CancelSaleResponse {
    private String message;

    public CancelSaleResponse(String message) {
        this.message = message; 
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
