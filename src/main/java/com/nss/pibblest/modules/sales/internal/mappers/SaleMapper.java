package com.nss.pibblest.modules.sales.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nss.pibblest.modules.sales.api.dto.SaleDetailDto;
import com.nss.pibblest.modules.sales.api.dto.SaleDto;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleDetailEntity;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleEntity;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(source = "store.id", target = "storeId")
    SaleDto toDto(SaleEntity entity);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    SaleDetailDto toDetailDto(SaleDetailEntity entity);
}
