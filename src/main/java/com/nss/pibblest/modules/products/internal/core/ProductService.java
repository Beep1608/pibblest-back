package com.nss.pibblest.modules.products.internal.core;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;

public class ProductService {
    private final ProductRespository productRespository;

    public ProductService(ProductRespository productRespository){
        this.productRespository = productRespository;
    }
}
