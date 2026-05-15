package com.nss.pibblest.modules.products.internal.core;

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

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    private final ProductRespository productRespository;
    private final StoreProductRepository storeProductRepository;

    private final ProductMapper productMapper;
    public ProductService(ProductRespository productRespository,StoreProductRepository storeProductRepository ,ProductMapper productMapper){
        this.productRespository = productRespository;
        this.storeProductRepository = storeProductRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ResponseEntity<CreateProductResponse> createProduct(CreateProductRequest request){

        ProductEntity productRequest = productMapper.toEntity(request);
        ProductEntity newProduct =  productRespository.save(productRequest);

        CreateProductResponse response = new CreateProductResponse(newProduct.getId(), "Su producto fue registrado");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<GetProductsFromStoreResponse> getProductsFromStore(Long storeId, Pageable pageable){
        Page<StoreProductEntity> storeProductEntitys=  storeProductRepository.findByStoreIdAndIsActiveTrue(storeId, pageable);
        Page<ProductPreviewDto> productEntitys = storeProductEntitys.map(spe -> productMapper.toPreviewDto( spe.getProduct()));
        GetProductsFromStoreResponse response = new GetProductsFromStoreResponse(productEntitys);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
