package com.nss.pibblest.modules.stores.internal.web.requests.createStore;

public class CreateStoreResponse {
    private String message;

    public CreateStoreResponse(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
}
