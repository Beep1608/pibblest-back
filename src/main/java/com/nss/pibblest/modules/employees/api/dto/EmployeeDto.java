package com.nss.pibblest.modules.employees.api.dto;

import java.util.List;
import java.util.UUID;
import com.nss.pibblest.shared.Role;

public record EmployeeDto(
    UUID id,
    String name,
    String lastName,
    String username,
    Role role,
    // Eliminamos Set<Permission> y List<Long> storeIds, y ponemos la nueva estructura:
    List<StoreAssignmentDto> storeAssignments 
) {}
