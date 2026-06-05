package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    Page<EmployeeEntity> findByDeletedAtIsNull(Pageable pageable);
    Optional<EmployeeEntity> findByUsernameAndDeletedAtIsNull(String username);
    boolean existsByUsernameAndDeletedAtIsNull(String username);
}
