package com.nss.pibblest.modules.owners.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nss.pibblest.modules.owners.api.OwnerDto;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.profile.OwnerProfileResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.UpdateProfileRequest;

@Mapper(componentModel="spring")
public interface OwnerMapper {
    
    OwnerDto toDto(OwnerEntity entity);
    
    OwnerProfileResponse toProfileResponse(OwnerEntity entity);

    @Mapping(target= "id", ignore=true)
    @Mapping(target= "isActive", ignore=true)
    @Mapping(target= "lastLogin", ignore=true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    @Mapping(target= "verifiedAt", ignore=true)
    @Mapping(target= "organizationCode", ignore=true)
    @Mapping(target= "schemaName", ignore=true)
    OwnerEntity toEntity(CreateOwnerRequest request);

    // Actualiza una entidad existente ignorando los campos críticos
    @Mapping(target= "id", ignore=true)
    @Mapping(target= "company", ignore=true)
    @Mapping(target= "email", ignore=true)
    @Mapping(target= "password", ignore=true)
    @Mapping(target= "isActive", ignore=true)
    @Mapping(target= "lastLogin", ignore=true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    @Mapping(target= "verifiedAt", ignore=true)
    @Mapping(target= "organizationCode", ignore=true)
    @Mapping(target= "schemaName", ignore=true)
    void updateEntityFromRequest(UpdateProfileRequest request, @MappingTarget OwnerEntity entity);
}
