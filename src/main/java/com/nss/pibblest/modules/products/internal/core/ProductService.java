package com.nss.pibblest.modules.products.internal.core;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.products.api.ProductPreviewDto;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;
import com.nss.pibblest.modules.products.internal.mappers.ProductMapper;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.getProductsFromStore.GetProductsFromStoreResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductRepository;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.mappers.TagMapper;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    private final ProductRespository productRespository;
    private final StoreProductRepository storeProductRepository;

    private final ProductMapper productMapper;
    private final TagMapper tagMapper;

    public ProductService(ProductRespository productRespository, StoreProductRepository storeProductRepository,
            ProductMapper productMapper, TagMapper tagMapper) {
        this.productRespository = productRespository;
        this.storeProductRepository = storeProductRepository;
        this.productMapper = productMapper;
        this.tagMapper = tagMapper;
    }

    @Transactional
    public ResponseEntity<CreateProductResponse> createProduct(CreateProductRequest request) {

        ProductEntity productRequest = productMapper.toEntity(request);
        ProductEntity newProduct = productRespository.save(productRequest);

        CreateProductResponse response = new CreateProductResponse(newProduct.getId(), "Su producto fue registrado");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<GetProductsFromStoreResponse> getProductsFromStore(Long storeId, String keyword,
            Pageable pageable) {
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

            products = productRespository.findByName(keyword, pageable);
        }

        Page<ProductPreviewDto> productsDto = products.map(p -> productMapper.toPreviewDto(p));

        GetProductsFromStoreResponse response = new GetProductsFromStoreResponse(productsDto);

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
}
