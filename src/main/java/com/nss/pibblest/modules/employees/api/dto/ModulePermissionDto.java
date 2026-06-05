package com.nss.pibblest.modules.employees.api.dto;

import java.util.List;

public record ModulePermissionDto(
    Long moduleId,
    String moduleCode, // Ej: MODULE_SALES
    List<String> actions
) {}
