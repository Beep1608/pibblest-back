package com.nss.pibblest.modules.tags.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.api.dto.TagForProductDto;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsEntity;

@Mapper(componentModel="spring")
public interface TagMapper  {
    
    @Mapping(target = "usageCount", constant = "0L")
    TagDto toDto(TagEntity entity);

    @Mapping(target = "usageCount", constant = "0L")
    TagDto fromTagForProductToDto(TagForProductsEntity entity);

    @Mapping(target = "usageCount", constant = "0L")
    TagForProductDto toTagForProductDto(TagForProductsEntity entity);
}
