package com.nss.pibblest.modules.owners.api;

import java.util.UUID;

public record OwnerDto(
    UUID id,
    String company,
    String name,
    String lastName,
    String email, 
    String organizationCode
){}
