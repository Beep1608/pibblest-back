package com.nss.pibblest.modules.products.internal.web.requests.createProduct;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record AssignProductsToStoreRequest(
    @NotEmpty(message = "{product.validation.assignment.items.required}")
    @Valid
    List<ProductAssignmentItem> items
) {
    public record ProductAssignmentItem(
        @NotNull(message = "{product.validation.id.required}")
        Long productId,
        
        @NotNull(message = "{product.validation.quantity.required}")
        @Positive(message = "{product.validation.quantity.positive}")
        Long quantity
    ) {}
}
