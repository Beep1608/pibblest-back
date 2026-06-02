package com.nss.pibblest.modules.stores.internal.web.requests.updateStore;

import java.util.Set;

public class UpdateStoreRequest {

    private String name;
    private String address;
    private String status;
    private Set<Long> tagsId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Long> getTagsId() {
        return tagsId;
    }

    public void setTagsId(Set<Long> tagsId) {
        this.tagsId = tagsId;
    }
}
