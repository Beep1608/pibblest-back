package com.nss.pibblest.modules.tags.internal.web.requests.products;

public class AssignTagToProductResponse {
    private String message;

    public AssignTagToProductResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
