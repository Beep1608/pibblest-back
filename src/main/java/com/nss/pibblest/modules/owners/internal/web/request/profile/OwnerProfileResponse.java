package com.nss.pibblest.modules.owners.internal.web.request.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.UUID;

@Schema(description = "Datos públicos del perfil del Owner")
public record OwnerProfileResponse(
    UUID id,
    String company,
    String name,
    String lastName,
    String email,
    String organizationCode,
    boolean isActive,
    ZonedDateTime verifiedAt
) {}
