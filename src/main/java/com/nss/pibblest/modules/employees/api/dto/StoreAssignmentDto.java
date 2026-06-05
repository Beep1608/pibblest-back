package com.nss.pibblest.modules.employees.api.dto;

import java.util.List;

public record StoreAssignmentDto(
    Long storeId,
    String storeName,
    List<ModulePermissionDto> modules
) {}
