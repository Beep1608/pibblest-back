package com.nss.pibblest.modules.owners.api.events;

import org.springframework.modulith.events.Externalized;

@Externalized("tenant-schema-ready-topic")
public record TenantSchemaReadyEvent(
    String schemaName,
    String email,
    String encodedPassword,
    String name,
    String lastName
) {}
