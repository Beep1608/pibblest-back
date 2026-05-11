package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface  EmployeeStoreRepository extends JpaRepository<EmployeeStoreEntity, Long> {
    
}
