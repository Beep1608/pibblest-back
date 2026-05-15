package com.nss.pibblest.modules.tags.internal.infrastructure.data.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  TagProductRepository extends JpaRepository<TagProductEntity, TagProductEmbedded> {
    
}
