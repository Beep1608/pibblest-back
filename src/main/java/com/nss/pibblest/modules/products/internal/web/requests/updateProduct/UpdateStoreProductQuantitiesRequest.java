package com.nss.pibblest.modules.products.internal.web.requests.updateProduct;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateStoreProductQuantitiesRequest(
    @NotNull(message = "{product.validation.desiredQuantity.required}")
    @PositiveOrZero(message = "{product.validation.desiredQuantity.positive}")
    Long desiredQuantity,

    @NotNull(message = "{product.validation.currentQuantity.required}")
    @PositiveOrZero(message = "{product.validation.currentQuantity.positive}")
    Long currentQuantity
) {}
