package com.nss.pibblest.modules.sales.internal.infrastructure.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  SaleRepository extends  JpaRepository<SaleEntity, Long>

{
    
}
