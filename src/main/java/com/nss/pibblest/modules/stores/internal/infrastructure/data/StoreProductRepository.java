package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProductEntity, StoreProductId> {

    Page<StoreProductEntity> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    List<StoreProductEntity> findByStoreIdAndIsActiveTrue(Long storeId);

    List<StoreProductEntity> findByStoreIdAndProductIdIn(Long storeId, List<Long> productIds);

}
