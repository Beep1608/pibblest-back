package com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct;

import java.util.Set;

public class AssignTagToProductRequest {
    private Long productId;
    private Set<Long> tagsId;
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Set<Long> getTagsId() {
        return tagsId;
    }

    public void setTagsId(Set<Long> tagsId) {
        this.tagsId = tagsId;
    }


    
}
