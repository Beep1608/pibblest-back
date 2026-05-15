package com.nss.pibblest.modules.tags.internal.infrastructure.data.products;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name="products_tags")
public class TagProductEntity {
    
    @EmbeddedId
    private TagProductEmbedded id =new TagProductEmbedded();

    @ManyToMany(fetch=FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name="product_id")
    private ProductEntity productEntity;

    @ManyToMany(fetch=FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name="tag_id")
    private TagForProductsEntity tagForProductsEntity;

    public TagProductEntity() {
    }

    public TagProductEntity(ProductEntity productEntity, TagForProductsEntity tagForProductsEntity) {
        this.productEntity = productEntity;
        this.tagForProductsEntity = tagForProductsEntity;
    }

    public TagProductEmbedded getId() {
        return id;
    }

    public void setId(TagProductEmbedded id) {
        this.id = id;
    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }

    public TagForProductsEntity getTagForProductsEntity() {
        return tagForProductsEntity;
    }

    public void setTagForProductsEntity(TagForProductsEntity tagForProductsEntity) {
        this.tagForProductsEntity = tagForProductsEntity;
    }
}
