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
            // 2. Ejecutamos la consulta. Tu TenantConnectionProvider inyectará 
            // el SET search_path TO "esquema_del_owner" correctamente.
            List<EmployeeEntity> result = em.createQuery(
                    "SELECT e FROM EmployeeEntity e WHERE e.username = :username", 
                    EmployeeEntity.class)
                    .setParameter("username", username)
                    .getResultList();
            
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
            
        } finally {
            // 3. Cerramos el EntityManager. Tu TenantConnectionProvider invocará 
            // releaseConnection y hará el SET search_path TO identity, devolviendo la conexión limpia al pool.
            em.close(); 
        }
    }
}