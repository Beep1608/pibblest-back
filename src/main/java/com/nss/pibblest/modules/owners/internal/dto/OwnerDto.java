package com.nss.pibblest.modules.owners.internal.dto;

import java.util.UUID;

public record OwnerDto(
    UUID id,
    String firstName,
    String lastName,
    String email, 
    String organizationCode
){}
