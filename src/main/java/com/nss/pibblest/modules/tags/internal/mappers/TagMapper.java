package com.nss.pibblest.modules.tags.internal.mappers;

import org.mapstruct.Mapper;

import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.api.dto.TagForProductDto;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsEntity;

@Mapper(componentModel="spring")
public interface TagMapper  {
    
    TagDto toDto(TagEntity entity);

    TagDto fromTagForProductToDto(TagForProductsEntity entity);

    TagForProductDto toTagForProductDto(TagForProductsEntity entity);
}
