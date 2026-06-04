package com.nss.pibblest.modules.tags.internal.infrastructure.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nss.pibblest.modules.tags.api.dto.TagDto;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findById(Long id);

    List<TagEntity> findByDeletedAtIsNull();

    // Listado paginado con conteo optimizado y seguro para llaves compuestas
    @Query("""
     SELECT new com.nss.pibblest.modules.tags.api.dto.TagDto(
        t.name,
        t.id,
        COUNT(st.storeEntity.id)
     )
     FROM TagEntity t
     LEFT JOIN StoreTagEntity st ON st.tagEntity.id = t.id
     WHERE t.deletedAt IS NULL
     GROUP BY t.id, t.name
    """)
    Page<TagDto> findAllWithUsageCount(Pageable pageable);

    @Query("""
     SELECT new com.nss.pibblest.modules.tags.api.dto.TagDto(
        t.name,
        t.id,
        COUNT(st.storeEntity.id)
     )
     FROM TagEntity t
     LEFT JOIN StoreTagEntity st ON st.tagEntity.id = t.id
     WHERE t.deletedAt IS NULL AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
     GROUP BY t.id, t.name
    """)
    Page<TagDto> findByNameWithUsageCountContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
}

