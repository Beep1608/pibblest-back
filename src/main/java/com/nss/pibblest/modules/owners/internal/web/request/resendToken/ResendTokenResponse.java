package com.nss.pibblest.modules.owners.internal.web.request.resendToken;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar tras solicitar un nuevo token")
public record ResendTokenResponse(
    
    @Schema(description="Mensaje de confirmación genérico por razones de seguridad")
    String message

) {}
