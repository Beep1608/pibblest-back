package com.nss.pibblest.modules.tags.internal.infrastructure.data.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  TagForProductsRepository extends JpaRepository<TagForProductsEntity, Long> {
    Page<TagForProductsEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
