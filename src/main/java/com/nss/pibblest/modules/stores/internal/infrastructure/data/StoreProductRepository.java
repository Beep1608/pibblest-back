package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  StoreProductRepository extends  JpaRepository<StoreProductEntity, StoreProductId>{

    List<StoreProductEntity> findByStoreIdAndIsActiveTrue(Long storeId);
    List<StoreProductEntity> findByStoreIdAndProductIdIn(Long storeId, List<Long> productIds);
    
}
