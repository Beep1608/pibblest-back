package com.nss.pibblest.modules.swagger;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Pibblest API", 
        version = "1.0", 
        description = "Documentación interactiva de la API"
    ),
    // Esta línea le dice a Swagger que exija este esquema de seguridad por defecto en todas las rutas
    security = @SecurityRequirement(name = "bearerAuth") 
)
@SecurityScheme(
    name = "bearerAuth", // Este nombre debe coincidir con el de arriba
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Pega aquí tu token JWT. No necesitas escribir 'Bearer ' antes del token, Swagger lo hace por ti."
)
public class SwaggerConfig {
    // No necesitas escribir código aquí dentro, las anotaciones hacen toda la magia.
}