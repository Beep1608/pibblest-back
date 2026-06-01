package com.nss.pibblest.modules.products.internal.web.requests.updateProduct;

import com.nss.pibblest.modules.products.api.ProductPreviewDto;

public class UpdateProductResponse {
    String message;
    ProductPreviewDto dto;
    public UpdateProductResponse(String message, ProductPreviewDto dto) {
        this.message = message;
        this.dto = dto;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public ProductPreviewDto getDto() {
        return dto;
    }
    public void setDto(ProductPreviewDto dto) {
        this.dto = dto;
    }

    
    
}
