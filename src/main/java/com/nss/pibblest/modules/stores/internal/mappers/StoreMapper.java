package com.nss.pibblest.modules.stores.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.web.requests.createStore.CreateStoreRequest;

@Mapper(componentModel="spring")
public interface  StoreMapper {
    

    @Mapping(target="id", ignore=true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    @Mapping(target= "deletedAt", ignore=true)
    StoreEntity toEntity(CreateStoreRequest request);
}
