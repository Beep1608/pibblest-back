package com.nss.pibblest.modules.stores.internal.web.requests.storeProducts.asignProductToStore;

public class AsignStoreProductToStoreResponse {
    private String message ;

    public AsignStoreProductToStoreResponse(String message){
        this.message = message;
    }
    public AsignStoreProductToStoreResponse(){}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
