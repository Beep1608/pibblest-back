package com.nss.pibblest.modules.employees.api.dto;

import java.util.UUID;

public record EmployeeSimpleDto(
    UUID id,
    String username
) {}
