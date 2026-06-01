package com.nss.pibblest.modules.owners.internal.web.request.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para actualizar la información básica del perfil")
public record UpdateProfileRequest(
    
    @NotBlank(message = "{validation.owner.name.notblank}")
    @Schema(description="Nuevo nombre del propietario", example="Gru")
    String name,

    @NotBlank(message = "{validation.owner.lastname.notblank}")
    @Schema(description="Nuevo apellido del propietario", example="Felonius")
    String lastName

) {}
