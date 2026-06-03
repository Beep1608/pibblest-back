package com.nss.pibblest.modules.tags.api.dto;

public record TagDto (
    String name,
    Long id,
    Long usageCount // Nuevo campo para el conteo de usos
){}
