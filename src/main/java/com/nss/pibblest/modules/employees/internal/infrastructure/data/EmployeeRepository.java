package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nss.pibblest.modules.employees.api.dto.EmployeeSimpleDto;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    Page<EmployeeEntity> findByDeletedAtIsNull(Pageable pageable);
    Optional<EmployeeEntity> findByUsernameAndDeletedAtIsNull(String username);
    boolean existsByUsernameAndDeletedAtIsNull(String username);

    // Consulta para listar únicamente a los empleados que pertenecen a las tiendas permitidas
    @Query("SELECT DISTINCT e FROM EmployeeEntity e JOIN e.employeeStores es WHERE e.deletedAt IS NULL AND es.store.id IN :storeIds AND es.isActive = true")
    Page<EmployeeEntity> findByEmployeeStoresStoreIdInAndDeletedAtIsNull(@Param("storeIds") Set<Long> storeIds, Pageable pageable);

    // NUEVA CONSULTA: Proyección ultraligera de empleados activos por sucursal
    @Query("SELECT new com.nss.pibblest.modules.employees.api.dto.EmployeeSimpleDto(e.id, e.username) " +
           "FROM EmployeeEntity e JOIN e.employeeStores es " +
           "WHERE e.deletedAt IS NULL AND es.store.id = :storeId AND es.isActive = true")
    List<EmployeeSimpleDto> findSimpleEmployeesByStoreId(@Param("storeId") Long storeId);
}
