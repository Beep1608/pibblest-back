package com.nss.pibblest.modules.tags.internal.web.requests.assignTag;

import java.util.Set;

public class AssignTagRequest {
    
    private Long storeId;

    private Set<Long> tagsId;



    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Set<Long> getTagsId() {
        return tagsId;
    }

    public void setTagsId(Set<Long> tagsId) {
        this.tagsId = tagsId;
    }

   
    
}
