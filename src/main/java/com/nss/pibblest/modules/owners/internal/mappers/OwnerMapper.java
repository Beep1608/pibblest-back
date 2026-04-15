package com.nss.pibblest.modules.owners.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nss.pibblest.modules.owners.internal.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.dto.OwnerDto;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;

@Mapper(componentModel="spring")
public interface  OwnerMapper {
    
    OwnerDto toDto(OwnerEntity entity);

    @Mapping(target= "id", ignore=true)
    @Mapping(target= "password", ignore=true)
    @Mapping(target= "isActive", ignore=true)
    @Mapping(target= "lastLogin", ignore=true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    OwnerEntity toEntity(CreateOwnerRequest request);


}
