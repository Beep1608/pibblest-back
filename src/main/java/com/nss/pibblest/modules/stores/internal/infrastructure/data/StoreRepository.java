package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface  StoreRepository extends JpaRepository<StoreEntity, Long> {
    Optional<StoreEntity>  findById(Long id);

   // @Query("SELECT sp.product FROM StoreProductEntity sp WHERE sp.store.id = :storeId AND sp.isActive = true")
   // List<ProductEntity> findActiveProductsByStoreId(@Param("storeId") Long storeId);
    

}
