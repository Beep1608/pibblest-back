package com.nss.pibblest.modules.products.internal.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.products.api.ProductPreviewDto;
import com.nss.pibblest.modules.products.internal.core.exceptions.ProductNotFoundException;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;
import com.nss.pibblest.modules.products.internal.mappers.ProductMapper;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.deleteProduct.DeleteProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.getProductsFromStore.GetProductsFromStoreResponse;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductRepository;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.core.exceptions.TagsNotFound;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagProductEntity;
import com.nss.pibblest.modules.tags.internal.mappers.TagMapper;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    
    private final ProductRespository productRespository;
    private final StoreProductRepository storeProductRepository;
    private final ProductMapper productMapper;
    private final TagMapper tagMapper;
    private final TagForProductsRepository tagForProductsRepository;
    private final MessageSource messageSource;

    public ProductService(ProductRespository productRespository, StoreProductRepository storeProductRepository,
            ProductMapper productMapper, TagMapper tagMapper,
            TagForProductsRepository tagForProductsRepository, MessageSource messageSource) {
        this.productRespository = productRespository;
        this.storeProductRepository = storeProductRepository;
        this.productMapper = productMapper;
        this.tagMapper = tagMapper;
        this.tagForProductsRepository = tagForProductsRepository;
        this.messageSource = messageSource;
    }

    @Transactional
    public ResponseEntity<CreateProductResponse> createProduct(CreateProductRequest request) {
        ProductEntity productRequest = productMapper.toEntity(request);
        
        // 1. Guardar primero el producto base para obtener su ID
        ProductEntity newProduct = productRespository.save(productRequest);

        // 2. Asociar Tags si existen
        if (request.getTagsId() != null && !request.getTagsId().isEmpty()) {
            validateTags(request.getTagsId());
            for (Long tagId : request.getTagsId()) {
                TagForProductsEntity tag = tagForProductsRepository.getReferenceById(tagId);
                newProduct.getProductTags().add(new TagProductEntity(newProduct, tag));
            }
            // Guardar nuevamente para persistir las relaciones asociadas por cascade
            newProduct = productRespository.save(newProduct);
        }

        CreateProductResponse response = new CreateProductResponse(newProduct.getId(), "Su producto fue registrado");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<GetProductsFromStoreResponse> getProductsFromStore(Long storeId, String keyword, Pageable pageable) {
        Page<StoreProductEntity> storeProductEntities;
        if (keyword == null || keyword.trim().isEmpty()) {
            storeProductEntities = storeProductRepository.findByStoreIdAndIsActiveTrue(storeId, pageable);
        } else {
            storeProductEntities = storeProductRepository
                    .findByStoreIdAndProduct_NameContainingIgnoreCaseAndIsActiveTrue(storeId, keyword, pageable);
        }

        Page<ProductPreviewDto> productEntitys = storeProductEntities.map(spe -> {
            ProductEntity product = spe.getProduct();
            ProductPreviewDto dto = productMapper.toPreviewDto(product);
            dto.setCurrentQuantity(spe.getCurrentQuantity());
            dto.setDesiredQuantity(spe.getDesiredQuantity());

            if (product.getProductTags() != null) {
                List<TagDto> tags = product.getProductTags().stream()
                        .map(tagProduct -> tagMapper.fromTagForProductToDto(tagProduct.getTagForProductsEntity()))
                        .collect(Collectors.toList());
                dto.setTags(tags);
            }
            return dto;
        });
        
        GetProductsFromStoreResponse response = new GetProductsFromStoreResponse(productEntitys);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<GetProductsFromStoreResponse> getAllProducts(String keyword, Pageable pageable) {
        Page<ProductEntity> products;
        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRespository.findAll(pageable);
        } else {
            products = productRespository.findByNameContainingIgnoreCase(keyword, pageable);
        }

        Page<ProductPreviewDto> productsDto = products.map( p -> {
            
            ProductPreviewDto dto = productMapper.toPreviewDto(p);

            if (p.getProductTags() != null) {
                List<TagDto> tags = p.getProductTags().stream()
                        .map(tagProduct -> tagMapper.fromTagForProductToDto(tagProduct.getTagForProductsEntity()))
                        .collect(Collectors.toList());
                dto.setTags(tags);
            }
            return dto;
        });
        GetProductsFromStoreResponse response = new GetProductsFromStoreResponse(productsDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<ProductPreviewDto> getProductById(Long id) {
        // Usamos el nuevo método findByIdWithTags en lugar de findById
        ProductEntity product = productRespository.findByIdWithTags(id)
                .orElseThrow(ProductNotFoundException::new);
        
        // El mapper ahora rellenará automáticamente la lista 'tags' del DTO
        ProductPreviewDto dto = productMapper.toPreviewDto(product);
        
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @Transactional
    public ResponseEntity<UpdateProductResponse> editProduct(Long id, UpdateProductRequest request) {
        ProductEntity product = productRespository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        productMapper.updateEntityFromRequest(request, product);

        // Diffing algorithm para actualizar Tags asegurando orphan removal
        Set<Long> requestedTags = request.getTagsId() == null ? new HashSet<>() : request.getTagsId();
        
        if (!requestedTags.isEmpty()) {
            validateTags(requestedTags);
        }

        // 1. Eliminar tags desmarcados
        product.getProductTags().removeIf(tagProd -> !requestedTags.contains(tagProd.getTagForProductsEntity().getId()));

        // 2. Encontrar qué tags ya están asociados para no duplicar inserciones
        Set<Long> existingTagIds = product.getProductTags().stream()
                .map(tagProd -> tagProd.getTagForProductsEntity().getId())
                .collect(Collectors.toSet());

        // 3. Agregar los nuevos tags
        for (Long tagId : requestedTags) {
            if (!existingTagIds.contains(tagId)) {
                TagForProductsEntity tag = tagForProductsRepository.getReferenceById(tagId);
                product.getProductTags().add(new TagProductEntity(product, tag));
            }
        }

        product = productRespository.save(product);

        ProductPreviewDto dto = productMapper.toPreviewDto(product);
        UpdateProductResponse response = new UpdateProductResponse("product.updated.success", dto);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<DeleteProductResponse> deleteProduct(Long id) {
        ProductEntity product = productRespository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        productRespository.delete(product); // Eliminará cascada en products_tags gracias a CascadeType.ALL

        DeleteProductResponse response = new DeleteProductResponse("product.deleted.success");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private void validateTags(Set<Long> tagsId) {
        if (tagsId == null || tagsId.isEmpty()) return;

        List<TagForProductsEntity> existingTags = tagForProductsRepository.findAllById(tagsId);

        // Validar existencia y que no estén eliminados lógicamente
        List<TagForProductsEntity> activeTags = existingTags.stream()
            .filter(t -> t.getDeletedAt() == null)
            .collect(Collectors.toList());

        if (activeTags.size() != tagsId.size()) {
            Set<Long> foundIds = activeTags.stream()
                .map(TagForProductsEntity::getId)
                .collect(Collectors.toSet());
                
            Set<Long> missingIds = new HashSet<>(tagsId);
            missingIds.removeAll(foundIds);
            
            String errorMessage = messageSource.getMessage("error.tags.not.found",
                    new Object[] { missingIds.toString() }, LocaleContextHolder.getLocale());
            throw new TagsNotFound(errorMessage);
        }
    }
}
