package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


@Embeddable
public class StoreProductId implements  Serializable {

    @Column(name="store_id")
    private Long store_id;

    @Column(name="product_id")
    private Long product_id;


    public StoreProductId(Long store_id, Long product_id) {
        this.store_id = store_id;
        this.product_id = product_id;
    }

    public StoreProductId() {
    }
    
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if  ( o == null || getClass() != o.getClass()) return false;

        StoreProductId that = (StoreProductId) o;
        return Objects.equals(store_id, that.getStore_id()) &&
                Objects.equals(product_id, that.getProduct_id());
    }
        @Override
    public int hashCode(){
        return Objects.hash(store_id, product_id);
    }



    public Long getStore_id() {
        return store_id;
    }


    public void setStore_id(Long store_id) {
        this.store_id = store_id;
    }


    public Long getProduct_id() {
        return product_id;
    }


    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }
    
}
