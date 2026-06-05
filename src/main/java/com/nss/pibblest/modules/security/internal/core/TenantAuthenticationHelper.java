package com.nss.pibblest.modules.security.internal.core;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@Service
public class TenantAuthenticationHelper {

    private final EntityManagerFactory entityManagerFactory;

    public TenantAuthenticationHelper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public Optional<EmployeeEntity> findEmployeeByUsername(String username) {
        
        // 1. Creamos un EntityManager 100% nuevo. Al ser instanciado, 
        // Hibernate se ve forzado a invocar tu CurrentTenantResolver (que ahora ya tiene el nuevo schema).
        EntityManager em = entityManagerFactory.createEntityManager();
        
        try {
            // 2. Ejecutamos la consulta usando LEFT JOIN FETCH para evitar el LazyInitializationException
            // y usamos DISTINCT para que no nos devuelva empleados duplicados en la lista.
            // Además, validamos que no sea un empleado con borrado lógico (deletedAt IS NULL).
            List<EmployeeEntity> result = em.createQuery(
                    "SELECT DISTINCT e FROM EmployeeEntity e " +
                    "LEFT JOIN FETCH e.granularPermissions gp " +
                    "LEFT JOIN FETCH gp.store " +
                    "LEFT JOIN FETCH gp.module " +
                    "WHERE e.username = :username AND e.deletedAt IS NULL", 
                    EmployeeEntity.class)
                    .setParameter("username", username)
                    .getResultList();
            
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
            
        } finally {
            // 3. Cerramos el EntityManager.
            em.close(); 
        }
    }
}