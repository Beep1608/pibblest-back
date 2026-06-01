package com.nss.pibblest.modules.products.internal.web.requests.updateProduct;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateProductRequest {

    @NotBlank(message = "{product.validation.name.required}")
    private String name;

    @NotBlank(message = "{product.validation.sku.required}")
    private String sku;

    private String barcode;
    
    private String description;
    
    private String brand;

    @NotNull(message = "{product.validation.basePrice.required}")
    @PositiveOrZero(message = "{product.validation.basePrice.positive}")
    private BigDecimal basePrice;

    @NotNull(message = "{product.validation.cost.required}")
    @PositiveOrZero(message = "{product.validation.cost.positive}")
    private BigDecimal cost;

    @NotNull(message = "{product.validation.quantity.required}")
    @PositiveOrZero(message = "{product.validation.quantity.positive}")
    private Long quantity;

    public UpdateProductRequest() {
    }

    public UpdateProductRequest(String name, String sku, String barcode, String description, String brand, BigDecimal basePrice, BigDecimal cost, Long quantity) {
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.description = description;
        this.brand = brand;
        this.basePrice = basePrice;
        this.cost = cost;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
