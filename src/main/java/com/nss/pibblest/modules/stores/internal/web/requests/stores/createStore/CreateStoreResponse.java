package com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore;

import com.nss.pibblest.modules.stores.api.dtos.StoreDto;

public class CreateStoreResponse {
    private String message;
    private StoreDto store;

    public CreateStoreResponse(String message, StoreDto store){
        this.message = message;
        this.store = store;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StoreDto getStore() {
        return store;
    }

    public void setStore(StoreDto store) {
        this.store = store;
    }
}
