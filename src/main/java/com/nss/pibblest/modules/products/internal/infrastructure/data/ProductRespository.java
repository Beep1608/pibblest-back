package com.nss.pibblest.modules.products.internal.infrastructure.data;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRespository extends JpaRepository<ProductEntity, Long>{
    
    // Filtramos para que solo traiga los activos
    Page<ProductEntity> findByDeletedAtIsNull(Pageable pageable);

    // Filtramos búsqueda por nombre excluyendo eliminados
    Page<ProductEntity> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String keyword, Pageable pageable);

    // Método seguro para buscar por ID excluyendo eliminados
    Optional<ProductEntity> findByIdAndDeletedAtIsNull(Long id);

    // Actualizamos la consulta personalizada para excluir eliminados lógicamente
    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.productTags pt LEFT JOIN FETCH pt.tagForProductsEntity WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<ProductEntity> findByIdWithTags(@Param("id") Long id);
}
