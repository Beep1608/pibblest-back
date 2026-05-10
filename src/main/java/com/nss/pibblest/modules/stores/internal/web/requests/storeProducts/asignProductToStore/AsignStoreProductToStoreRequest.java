package com.nss.pibblest.modules.stores.internal.web.requests.storeProducts.asignProductToStore;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AsignStoreProductToStoreRequest {
    @NotNull(message = "No puede ser nulo")
    private Long storeId;
     @NotNull(message = "No puede ser nulo")
    private Long productId;

    @NotNull(message = "No puede ser nulo")
    @Positive(message = "La cantidad no puede ser negativa")
    private Long quantity;
    public Long getStoreId() {
        return storeId;
    }
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public Long getQuantity() {
        return quantity;
    }
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
