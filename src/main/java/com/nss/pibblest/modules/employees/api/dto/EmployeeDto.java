package com.nss.pibblest.modules.employees.api.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.nss.pibblest.shared.Permission;
import com.nss.pibblest.shared.Role;

public record EmployeeDto(
    UUID id,
    String name,
    String lastName,
    String username,
    Role role,
    Set<Permission> permissions,
    List<Long> storeIds
) {}
