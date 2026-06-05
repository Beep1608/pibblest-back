package com.nss.pibblest.modules.sales.internal.infrastructure.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<SaleEntity, Long> {
    
    Page<SaleEntity> findByDeletedAtIsNull(Pageable pageable);
    
    // Para el OWNER o empleados con permisos READ globales de la tienda
    Page<SaleEntity> findByStoreIdAndDeletedAtIsNull(Long storeId, Pageable pageable);

    // NUEVO: Para empleados que solo pueden ver sus propias ventas en esa tienda
    Page<SaleEntity> findByStoreIdAndEmployeeIdAndDeletedAtIsNull(Long storeId, UUID employeeId, Pageable pageable);

    Optional<SaleEntity> findByIdAndDeletedAtIsNull(Long id);
}
