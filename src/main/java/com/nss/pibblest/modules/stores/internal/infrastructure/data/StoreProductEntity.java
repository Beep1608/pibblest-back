package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name="stores_products" )
public class StoreProductEntity {
    @EmbeddedId
    private StoreProductId id = new StoreProductId();


    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("store_id")
    @JoinColumn(name="store_id")
    private StoreEntity store;


    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("product_id")
    @JoinColumn(name="product_id")
    private ProductEntity product;

    @Column(name="desiredquantity")
    private Long desiredquantity;



    @Column(name="currentquantity")
    private Long currentquantity;



    @Column(name="is_active")
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;






    public StoreProductEntity(StoreEntity store, ProductEntity product) {
        this.id = new StoreProductId(store.getId(), product.getId());
        this.store = store;
        this.product = product;
    }


    public StoreProductEntity() {
    }


    public StoreProductId getId() {
        return id;
    }


    public void setId(StoreProductId id) {
        this.id = id;
    }


    public StoreEntity getStore() {
        return store;
    }


    public void setStore(StoreEntity store) {
        this.store = store;
    }


    public ProductEntity getProduct() {
        return product;
    }


    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

       public Long getDesiredquantity() {
        return desiredquantity;
    }


    public void setDesiredquantity(Long desiredquantity) {
        this.desiredquantity = desiredquantity;
    }

    
    public Long getCurrentquantity() {
        return currentquantity;
    }


    public void setCurrentquantity(Long currentquantity) {
        this.currentquantity = currentquantity;
    }


}
