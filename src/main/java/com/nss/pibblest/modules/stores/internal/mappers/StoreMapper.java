package com.nss.pibblest.modules.stores.internal.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.nss.pibblest.modules.stores.api.dtos.StoreDto;
import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreRequest;

@Mapper(componentModel="spring")
public interface  StoreMapper {

    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateStoreRequest request, @MappingTarget StoreEntity entity);
    

    @Mapping(target="id", ignore=true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    @Mapping(target= "deletedAt", ignore=true)
    StoreEntity toEntity(CreateStoreRequest request);


    @Mapping(target="createdAt", source="createdAt", dateFormat="dd/MM/yyyy")
    StoreDto toDto(StoreEntity entity);

}
