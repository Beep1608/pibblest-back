package com.nss.pibblest.modules.owners.internal.web.request.resendToken;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para solicitar el reenvío del token de verificación")
public record ResendTokenRequest(
    
    @NotBlank(message="{validation.owner.email.notblank}")
    @Email(message="{validation.owner.email.format}")
    @Schema(description="Correo electrónico asociado a la cuenta", example="hola@example.com")
    String email

) {}
