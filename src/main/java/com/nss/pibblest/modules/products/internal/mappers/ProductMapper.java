package com.nss.pibblest.modules.products.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nss.pibblest.modules.products.api.ProductPreviewDto;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductRequest;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagProductEntity;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target= "createdAt", ignore=true)
    @Mapping(target= "updatedAt", ignore=true)
    @Mapping(target= "productTags", ignore=true)
    ProductEntity toEntity(CreateProductRequest request);

    // SOLUCIÓN: Le indicamos que mapee la lista usando nuestro método default
    @Mapping(target = "tags", source = "productTags")
    ProductPreviewDto toPreviewDto(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "productTags", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget ProductEntity entity);

    // Adaptamos el método para que devuelva TagDto como espera ProductPreviewDto
    default List<TagDto> mapProductTags(List<TagProductEntity> productTags) {
        if (productTags == null) return java.util.Collections.emptyList();
        
        return productTags.stream()
            .filter(tp -> tp.getTagForProductsEntity().getDeletedAt() == null) // Evitar mostrar tags borrados
            .map(tp -> new TagDto(
                    tp.getTagForProductsEntity().getName(), 
                    tp.getTagForProductsEntity().getId(), 
                    0L))
            .collect(Collectors.toList());
    }
}
