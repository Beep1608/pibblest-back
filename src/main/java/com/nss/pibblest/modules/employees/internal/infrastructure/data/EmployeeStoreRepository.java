package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface  EmployeeStoreRepository extends JpaRepository<EmployeeStoreEntity, Long> {

    List<EmployeeStoreEntity> findByEmployee_IdIn(List<UUID> employeeIds);

    
}
