package com.nss.pibblest.modules.products.api;

import java.math.BigDecimal;

public class ProductPreviewDto {
    private String name;
    private BigDecimal basePrice;
    private Long quantity;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    public Long getQuantity() {
        return quantity;
    }
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    
}
