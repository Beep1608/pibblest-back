package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProductEntity, StoreProductId> {

    @EntityGraph(attributePaths={"product"})
    Page<StoreProductEntity> findByStoreIdAndIsActiveTrue(Long storeId, Pageable pageable);

    @EntityGraph(attributePaths={"product"})
    Page<StoreProductEntity> findByStoreIdAndProduct_NameContainingIgnoreCaseAndIsActiveTrue(
        Long storeId,
        String keyword,
        Pageable pageable
    );

    List<StoreProductEntity> findByStoreIdAndIsActiveTrue(Long storeId);

    List<StoreProductEntity> findByStoreIdAndProductIdIn(Long storeId, List<Long> productIds);

}
