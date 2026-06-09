package com.nss.pibblest.modules.products.internal.web.requests.deleteProduct;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DissociateProductsRequest(
    @NotEmpty(message = "{product.validation.ids.required}")
    List<Long> productIds
) {}
