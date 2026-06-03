package com.nss.pibblest.modules.stores.internal.mappers;

import java.util.List;

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
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagEntity;

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
    @Mapping(target="tags", source="storeTags")
    StoreDto toDto(StoreEntity entity);

    default List<TagDto> mapStoreTags(List<StoreTagEntity> storeTags) {
        if (storeTags == null) return java.util.Collections.emptyList();
        return storeTags.stream()
            .map(st -> new TagDto(st.getTagEntity().getName(), st.getTagEntity().getId()))
            .toList();
    }
}
