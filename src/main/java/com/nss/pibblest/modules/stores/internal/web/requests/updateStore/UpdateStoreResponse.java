package com.nss.pibblest.modules.stores.internal.web.requests.updateStore;

public class UpdateStoreResponse {
    
    private String message;

    public UpdateStoreResponse(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
