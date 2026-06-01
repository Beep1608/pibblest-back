package com.nss.pibblest.modules.products.internal.web;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.products.api.ProductPreviewDto;
import com.nss.pibblest.modules.products.internal.core.ProductService;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.deleteProduct.DeleteProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.getProductsFromStore.GetProductsFromStoreResponse;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request){
        return this.productService.createProduct(request);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<GetProductsFromStoreResponse> getProductsFromStorePreview(
            @PathVariable("storeId") Long storeId, 
            @RequestParam(required=false) String keyword, 
            Pageable pageable){
        return productService.getProductsFromStore(storeId, keyword, pageable);
    }

    @GetMapping("/all")
    public ResponseEntity<GetProductsFromStoreResponse> getAll(
            @RequestParam(required = false) String keyword, 
            Pageable pageable){
        return productService.getAllProducts(keyword, pageable);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductPreviewDto> getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateProductResponse> editProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.editProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteProductResponse> deleteProduct(@PathVariable("id") Long id) {
        return productService.deleteProduct(id);
    }
}
