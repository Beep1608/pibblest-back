package com.nss.pibblest.modules.owners.internal.web.request.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para actualizar la información básica del perfil")
public record UpdateProfileRequest(
    
    @NotBlank(message = "{validation.owner.name.notblank}")
    @Size(max = 100, message = "{validation.owner.name.size}")
    @Schema(description="Nuevo nombre del propietario", example="Gru")
    String name,

    @NotBlank(message = "{validation.owner.lastname.notblank}")
    @Size(max = 100, message = "{validation.owner.lastname.size}")
    @Schema(description="Nuevo apellido del propietario", example="Felonius")
    String lastName

) {}
