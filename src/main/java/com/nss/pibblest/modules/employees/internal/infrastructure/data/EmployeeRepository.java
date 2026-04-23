package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    Optional<EmployeeEntity> findByUsername(String username);
    
}
