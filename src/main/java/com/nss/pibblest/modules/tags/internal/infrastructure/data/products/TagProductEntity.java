package com.nss.pibblest.modules.tags.internal.infrastructure.data.products;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name="products_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "tag_id"})
})
public class TagProductEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="product_id", nullable=false)
    private ProductEntity productEntity;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tag_id", nullable=false)
    private TagForProductsEntity tagForProductsEntity;

    public TagProductEntity() {
    }

    public TagProductEntity(ProductEntity productEntity, TagForProductsEntity tagForProductsEntity) {
        this.productEntity = productEntity;
        this.tagForProductsEntity = tagForProductsEntity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
