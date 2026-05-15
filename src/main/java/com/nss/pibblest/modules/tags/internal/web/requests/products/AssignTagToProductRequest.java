package com.nss.pibblest.modules.tags.internal.web.requests.products;

public class AssignTagToProductRequest {
    private Long productId;
    private Long tagId;
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public Long getTagId() {
        return tagId;
    }
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    
}
