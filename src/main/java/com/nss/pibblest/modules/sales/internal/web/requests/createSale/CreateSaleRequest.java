package com.nss.pibblest.modules.sales.internal.web.requests.createSale;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateSaleRequest {

    @NotNull(message = "El ID de la tienda es obligatorio")
    private Long storeId;

    @NotEmpty(message = "La venta debe contener al menos un producto")
    @Valid
    private List<SaleItemRequest> items;

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<SaleItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequest> items) {
        this.items = items;
    }

    public static class SaleItemRequest {
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productId;

        @NotNull(message = "La cantidad debe ser obligatoria")
        @Positive(message = "La cantidad vendida debe ser mayor a cero")
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

}
