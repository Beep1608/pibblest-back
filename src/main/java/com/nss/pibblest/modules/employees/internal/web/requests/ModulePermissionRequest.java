package com.nss.pibblest.modules.employees.internal.web.requests;

import java.util.List;

public record ModulePermissionRequest(
    Long moduleId,
    List<String> actions // Ej: ["CREATE", "READ"]
) {}
