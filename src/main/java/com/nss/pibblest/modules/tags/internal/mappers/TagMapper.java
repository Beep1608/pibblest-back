package com.nss.pibblest.modules.tags.internal.mappers;

import org.mapstruct.Mapper;

import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;

@Mapper(componentModel="spring")
public interface TagMapper  {
    
    TagDto toDto(TagEntity entity);

}
