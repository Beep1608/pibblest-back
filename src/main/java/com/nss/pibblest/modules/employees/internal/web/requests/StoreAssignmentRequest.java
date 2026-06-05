package com.nss.pibblest.modules.employees.internal.web.requests;

import java.util.List;

public record StoreAssignmentRequest(
    Long storeId,
    List<ModulePermissionRequest> permissions
) {}
