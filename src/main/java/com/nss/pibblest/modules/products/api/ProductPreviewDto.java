package com.nss.pibblest.modules.products.api;

import java.math.BigDecimal;
import java.util.List;

import com.nss.pibblest.modules.tags.api.dto.TagDto;

public class ProductPreviewDto {
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal cost;
    private Long quantity;
    private Long desiredQuantity;
    private Long currentQuantity;
    private String sku;
    private String barcode;
    private String description;
    private String brand;
    private List<TagDto> tags;
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
    public BigDecimal getCost() {
        return cost;
    }
    public void setCost(BigDecimal cost) {
        this.cost = cost;
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
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public Long getDesiredQuantity() {
        return desiredQuantity;
    }

    public void setDesiredQuantity(Long desiredQuantity) {
        this.desiredQuantity = desiredQuantity;
    }

    public Long getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(Long currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    
}
