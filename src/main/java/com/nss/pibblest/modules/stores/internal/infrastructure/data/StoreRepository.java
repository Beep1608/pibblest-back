package com.nss.pibblest.modules.stores.internal.infrastructure.data;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;


@Repository
public interface  StoreRepository extends JpaRepository<StoreEntity, Long> {
    Optional<StoreEntity>  findById(Long id);

   // @Query("SELECT sp.product FROM StoreProductEntity sp WHERE sp.store.id = :storeId AND sp.isActive = true")
   // List<ProductEntity> findActiveProductsByStoreId(@Param("storeId") Long storeId);
    @Query("""
     SELECT new com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto(
        s.id,
        s.name,
        s.address,
        s.status,
        COALESCE(SUM(sp.desiredQuantity), 0L),
        COALESCE(SUM(sp.currentQuantity), 0L),
        (SELECT COUNT(sa) FROM SaleEntity sa WHERE sa.store.id = s.id AND sa.createdAt >= :startOfDay),
        (SELECT SUM(sa.totalAmount) FROM SaleEntity sa WHERE sa.store.id = s.id),
        (SELECT COUNT(es) FROM EmployeeStoreEntity es WHERE es.store.id = s.id AND es.isActive = true),
        s.createdAt
     )        
    FROM StoreEntity s 
    LEFT JOIN StoreProductEntity sp ON sp.store.id = s.id AND sp.isActive = true

    GROUP BY s.id, s.name, s.address, s.status

    """)
    Page<StorePreviewDto> findStorePreviewInfo(Pageable pageable, @Param("startOfDay") ZonedDateTime startOfDay);


    /**
     * Consulta ultra rápida para recalcular en vivo los KPIs de una sola tienda.
     * Se usa para disparar las actualizaciones SSE.
     */
    @Query("""
     SELECT new com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto(
        s.id,
        s.name,
        s.address,
        s.status,
        COALESCE(SUM(sp.desiredQuantity), 0L),
        COALESCE(SUM(sp.currentQuantity), 0L),
        (SELECT COUNT(sa) FROM SaleEntity sa WHERE sa.store.id = s.id AND sa.createdAt >= :startOfDay),
        (SELECT SUM(sa.totalAmount) FROM SaleEntity sa WHERE sa.store.id = s.id),
        (SELECT COUNT(es) FROM EmployeeStoreEntity es WHERE es.store.id = s.id AND es.isActive = true),
        s.createdAt
     )        
    FROM StoreEntity s 
    LEFT JOIN StoreProductEntity sp ON sp.store.id = s.id AND sp.isActive = true
    WHERE s.id = :storeId
    GROUP BY s.id, s.name, s.address, s.status, s.createdAt
    """)
    Optional<StorePreviewDto> findStorePreviewById(
            @Param("storeId") Long storeId, 
            @Param("startOfDay") ZonedDateTime startOfDay
    );
}
