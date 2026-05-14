package com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore;

import com.nss.pibblest.shared.enums.StoreStatus;

public class CreateStoreRequest {
    private String name;
    private String address;
    private StoreStatus status = StoreStatus.ACTIVE;

    public StoreStatus getStatus() {
        return status;
    }

    public void setStatus(StoreStatus status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
