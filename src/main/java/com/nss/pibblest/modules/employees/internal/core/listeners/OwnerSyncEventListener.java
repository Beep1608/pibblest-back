package com.nss.pibblest.modules.employees.internal.core.listeners;

import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.owners.api.events.TenantSchemaReadyEvent;
import com.nss.pibblest.modules.tenant.TenantContext;
import com.nss.pibblest.shared.Permission;
import com.nss.pibblest.shared.Role;

@Component
public class OwnerSyncEventListener {

    private final EmployeeRepository employeeRepository;
    private final TransactionTemplate transactionTemplate;

    public OwnerSyncEventListener(EmployeeRepository employeeRepository, PlatformTransactionManager transactionManager) {
        this.employeeRepository = employeeRepository;
        // Configuramos la plantilla para que siempre abra una transacción NUEVA
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // NOTA: Se retira @Transactional de aquí para evitar que el proxy de Spring 
    // intercepte y abra la conexión antes de setear el esquema.
    @EventListener
    public void handleTenantSchemaReady(TenantSchemaReadyEvent event) {
        try {
            // 1. Seteamos el esquema en el contexto del hilo (ThreadLocal)
            TenantContext.setCurrentTenant(event.schemaName());
            
            // 2. AHORA abrimos la transacción de base de datos.
            // Al pedir la conexión, el pool leerá el TenantContext correcto.
            transactionTemplate.executeWithoutResult(status -> {
                EmployeeEntity ownerEmployee = new EmployeeEntity();
                ownerEmployee.setName(event.name());
                ownerEmployee.setLastName(event.lastName());
                ownerEmployee.setUsername(event.email());
                ownerEmployee.setPassword(event.encodedPassword());
                ownerEmployee.setRole(Role.OWNER);
                
                employeeRepository.save(ownerEmployee);
            });

        } finally {
            // 3. Limpieza segura del contexto
            TenantContext.clear();
        }
    }
}
