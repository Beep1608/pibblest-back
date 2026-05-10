package com.nss.pibblest.modules.products.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "id", ignore = true)
    //@Mapping(target = "tagId", ignore=true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    ProductEntity toEntity(CreateProductRequest request);
}
