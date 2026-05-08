package com.nss.pibblest.modules.products.internal.infrastructure.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  ProductRespository extends JpaRepository<ProductEntity, Long>{
    
}
