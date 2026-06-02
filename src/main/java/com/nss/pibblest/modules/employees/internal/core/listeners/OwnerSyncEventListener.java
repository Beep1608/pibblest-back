package com.nss.pibblest.modules.employees.internal.core.listeners;

import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;
import com.nss.pibblest.modules.tenant.TenantContext;
import com.nss.pibblest.shared.Permission;
import com.nss.pibblest.shared.Role;

@Component
public class OwnerSyncEventListener {

    private final EmployeeRepository employeeRepository;

    public OwnerSyncEventListener(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOwnerRegistered(OwnerRegisteredEvent event) {
        try {
            TenantContext.setCurrentTenant(event.schemaName());
            
            EmployeeEntity ownerEmployee = new EmployeeEntity();
            ownerEmployee.setUsername(event.email());
            ownerEmployee.setPassword(event.encodedPassword());
            ownerEmployee.setRole(Role.OWNER);
            ownerEmployee.setPermissions(Set.of(Permission.values()));
            
            employeeRepository.save(ownerEmployee);
        } finally {
            TenantContext.clear();
        }
    }
}
