package com.nss.pibblest.modules.products.internal.infrastructure.data;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  ProductRespository extends JpaRepository<ProductEntity, Long>{
    
    Page<ProductEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Nuevo método para evitar LazyInitializationException y traer los tags
    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.productTags pt LEFT JOIN FETCH pt.tagForProductsEntity WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithTags(@Param("id") Long id);
}
