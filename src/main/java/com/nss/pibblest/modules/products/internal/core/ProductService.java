package com.nss.pibblest.modules.products.internal.core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;
import com.nss.pibblest.modules.products.internal.mappers.ProductMapper;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductResponse;

@Service
public class ProductService {
    private final ProductRespository productRespository;

    private final ProductMapper productMapper;
    public ProductService(ProductRespository productRespository, ProductMapper productMapper){
        this.productRespository = productRespository;
        this.productMapper = productMapper;
    }

    public ResponseEntity<CreateProductResponse> createProduct(CreateProductRequest request){

        ProductEntity productRequest = productMapper.toEntity(request);
        ProductEntity newProduct =  productRespository.save(productRequest);

        CreateProductResponse response = new CreateProductResponse(newProduct.getId(), "Su producto fue registrado");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
