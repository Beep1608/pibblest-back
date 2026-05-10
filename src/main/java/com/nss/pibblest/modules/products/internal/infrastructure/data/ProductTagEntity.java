package com.nss.pibblest.modules.products.internal.infrastructure.data;

import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name= "products_tags", indexes={
    @Index(name="idx_tag_search", columnList="tag_id")
})
public class ProductTagEntity {
    
    @EmbeddedId
    private ProductTagId id = new ProductTagId();

    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name="product_id")
    private ProductEntity product;

    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name="tag_id")
    private TagEntity tag;

    public ProductTagEntity() {
    }

    public ProductTagEntity(ProductEntity product, TagEntity tag) {
        this.id = new ProductTagId(product.getId(), tag.getId());
        this.product = product;
        this.tag = tag;
    }

    public ProductTagId getId() {
        return id;
    }

    public void setId(ProductTagId id) {
        this.id = id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public TagEntity getTag() {
        return tag;
    }

    public void setTag(TagEntity tag) {
        this.tag = tag;
    }
}
