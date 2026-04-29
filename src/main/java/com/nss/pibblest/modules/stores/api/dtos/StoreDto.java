package com.nss.pibblest.modules.stores.api.dtos;

public record StoreDto(
    Long id,
    String name,
    String address,
    String status,
    String createdAt
){}