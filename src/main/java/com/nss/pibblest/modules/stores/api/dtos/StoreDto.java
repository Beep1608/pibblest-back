package com.nss.pibblest.modules.stores.api.dtos;

import java.util.List;
import com.nss.pibblest.modules.tags.api.dto.TagDto;

public record StoreDto(
    Long id,
    String name,
    String address,
    String status,
    String createdAt,
    List<TagDto> tags
){}