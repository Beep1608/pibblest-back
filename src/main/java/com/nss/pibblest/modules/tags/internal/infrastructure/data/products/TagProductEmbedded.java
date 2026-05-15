package com.nss.pibblest.modules.tags.internal.infrastructure.data.products;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TagProductEmbedded implements Serializable {
    
    @Column(name = "product_id")
    private Long productId;

    @Column(name="tag_id")
    private Long tagId;

    public TagProductEmbedded(){}
    public TagProductEmbedded(Long productId, Long tagId) {
        this.productId = productId;
        this.tagId = tagId;
    }

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
