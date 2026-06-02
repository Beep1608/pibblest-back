package com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore;

import java.util.Set;
import com.nss.pibblest.shared.StoreStatus;

public class CreateStoreRequest {
    private String name;
    private String address;
    private StoreStatus status = StoreStatus.ACTIVE;
    private Set<Long> tagsId;

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

    public Set<Long> getTagsId() {
        return tagsId;
    }

    public void setTagsId(Set<Long> tagsId) {
        this.tagsId = tagsId;
    }
}
