package com.nss.pibblest.modules.owners.internal.dto;

import java.util.UUID;

public record OwnerDto(
    UUID id,
    String company,
    String name,
    String lastName,
    String email, 
    String organizationCode
){}
