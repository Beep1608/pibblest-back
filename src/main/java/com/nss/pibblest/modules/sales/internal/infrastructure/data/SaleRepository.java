package com.nss.pibblest.modules.sales.internal.infrastructure.data;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<SaleEntity, Long> {
    Page<SaleEntity> findByDeletedAtIsNull(Pageable pageable);
    
    Page<SaleEntity> findByStoreIdAndDeletedAtIsNull(Long storeId, Pageable pageable);

    Optional<SaleEntity> findByIdAndDeletedAtIsNull(Long id);
}
