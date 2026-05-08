package com.nss.pibblest.modules.products.internal.infrastructure.data;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductTagId implements Serializable {
    

    @Column(name="product_id")
    private Long productId;

    @Column(name="tag_id")
    private Long tagId;

    public ProductTagId(Long productId, Long tagId) {
        this.productId = productId;
        this.tagId = tagId;
    }

    public ProductTagId(){}

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

    @Override
    public boolean equals (Object o){
        if (this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ProductTagId that = (ProductTagId) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(tagId,that.getTagId());


    }

    @Override
    public int hashCode(){
        return Objects.hash(productId, tagId);
    }

    

}
