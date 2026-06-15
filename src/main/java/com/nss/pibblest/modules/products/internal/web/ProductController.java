package com.nss.pibblest.modules.products.internal.web;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.AssignProductsResponse;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.AssignProductsToStoreRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.deleteProduct.DeleteProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.deleteProduct.DissociateProductsRequest;
import com.nss.pibblest.modules.products.internal.web.requests.getProductsFromStore.GetProductsFromStoreResponse;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateStoreProductQuantitiesRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateStoreProductQuantitiesResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request){
        return this.productService.createProduct(request);
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<GetProductsFromStoreResponse> getProductsFromStorePreview(
            @PathVariable("storeId") Long storeId, 
            @RequestParam(required=false) String keyword, 
            Pageable pageable){
        return productService.getProductsFromStore(storeId, keyword, pageable);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<GetProductsFromStoreResponse> getAll(
            @RequestParam(required = false) String keyword, 
            Pageable pageable){
        return productService.getAllProducts(keyword, pageable);
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ProductPreviewDto> getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/barcode/{code}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ProductPreviewDto> getProductByBarcode(@PathVariable("code") String code) {
        return productService.getProductByBarcode(code);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<UpdateProductResponse> editProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.editProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<DeleteProductResponse> deleteProduct(@PathVariable("id") Long id) {
        return productService.deleteProduct(id);
    }

    // NUEVO ENDPOINT (Regla 3): Actualizar stock deseado y actual de un producto en una sucursal específica
    @PutMapping("/store/{storeId}/quantities/{productId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<UpdateStoreProductQuantitiesResponse> updateStoreProductQuantities(
            @PathVariable("storeId") Long storeId,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateStoreProductQuantitiesRequest request) {
        return productService.updateStoreProductQuantities(storeId, productId, request);
    }

    // NUEVO ENDPOINT (Regla 4): Desvincular n cantidad de productos de una sucursal determinada
    @DeleteMapping("/store/{storeId}/associations")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<DeleteProductResponse> dissociateProducts(
            @PathVariable("storeId") Long storeId,
            @Valid @RequestBody DissociateProductsRequest request) {
        return productService.dissociateProductsFromStore(storeId, request);
    }

    // NUEVO ENDPOINT (Regla 5): Asociar masivamente n productos a una sucursal específica
    @PostMapping("/store/{storeId}/associate")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<AssignProductsResponse> assignProductsToStore(
            @PathVariable("storeId") Long storeId,
            @Valid @RequestBody AssignProductsToStoreRequest request) {
        return productService.assignProductsToStore(storeId, request);
    }
}
