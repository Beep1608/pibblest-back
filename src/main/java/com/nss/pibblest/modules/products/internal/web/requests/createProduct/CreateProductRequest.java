package com.nss.pibblest.modules.products.internal.web.requests.createProduct;

import java.math.BigDecimal;

public class CreateProductRequest {
      
    private String name;
    private String sku;
    private String barcode;
    private String description;
    private String brand;
    private BigDecimal basePrice;
    private BigDecimal cost;
    private Long quantity;
    public CreateProductRequest(String name, String sku, String barcode, String description, String brand,
            BigDecimal basePrice, BigDecimal cost, Long quantity) {
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
