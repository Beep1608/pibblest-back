package com.nss.pibblest.modules.stores.api.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class StorePreviewDto {

    private Long id;
    private String name;
    private String address;
    private String status;
    private Long totalProduct;
    private Long currentQuantityOfProducts;
    private Long salesOfToday;
    private BigDecimal totalSalesRevenue;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private ZonedDateTime createdAt;

    private Double growthFromStart;
    // int employees, // de la tabla stores_employees

    public StorePreviewDto(Long id, String name, String address, String status,
            Long totalProduct, Long currentQuantityOfProducts,
            Long salesOfToday, BigDecimal totalSalesRevenue,
            ZonedDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.totalProduct = totalProduct != null ? totalProduct : 0L;
        this.currentQuantityOfProducts = currentQuantityOfProducts != null ? currentQuantityOfProducts : 0L;
        this.salesOfToday = salesOfToday != null ? salesOfToday : 0L;
        this.totalSalesRevenue = totalSalesRevenue != null ? totalSalesRevenue : BigDecimal.ZERO;
        this.createdAt = createdAt;
        this.growthFromStart = calculateDailyGrowth();
    }

    public StorePreviewDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTotalProduct() {
        return totalProduct;
    }

    public void setTotalProduct(Long totalProduct) {
        this.totalProduct = totalProduct;
    }

    public Long getCurrentQuantityOfProducts() {
        return currentQuantityOfProducts;
    }

    public void setCurrentQuantityOfProducts(Long currentQuantityOfProducts) {
        this.currentQuantityOfProducts = currentQuantityOfProducts;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private Double calculateDailyGrowth() {
        if (createdAt == null || totalSalesRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        // 1. Calculamos los días de vida de la tienda
        long daysAlive = Duration.between(createdAt, ZonedDateTime.now()).toDays();

        // 2. Evitamos división por cero (si se creó hoy, contamos como 1 día)
        if (daysAlive <= 0)
            daysAlive = 1;

        // 3. Dividimos Total / Días (con 2 decimales)
        return totalSalesRevenue.divide(BigDecimal.valueOf(daysAlive), 2, RoundingMode.HALF_UP).doubleValue();
    }

    public Long getSalesOfToday() {
        return salesOfToday;
    }

    public void setSalesOfToday(Long salesOfToday) {
        this.salesOfToday = salesOfToday;
    }

    public Double getGrowthFromStart() {
        return growthFromStart;
    }

    public void setGrowthFromStart(Double growthFromStart) {
        this.growthFromStart = growthFromStart;
    }

    public BigDecimal getTotalSalesRevenue() {
        return totalSalesRevenue;
    }

    public void setTotalSalesRevenue(BigDecimal totalSalesRevenue) {
        this.totalSalesRevenue = totalSalesRevenue;
    }

}