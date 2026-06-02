package com.nss.pibblest.modules.stores.internal.web.requests.stores.deleteStore;

public class DeleteStoreResponse {
    private String message;

    public DeleteStoreResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
