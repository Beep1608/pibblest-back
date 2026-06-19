package com.nss.pibblest.modules.stores.api.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nss.pibblest.modules.tags.api.dto.TagDto;

public class StorePreviewDto {

    private Long id;
    private String name;
    private String address;
    private String status;
    private String timezone;
    private Long totalProducts;
    private Long currentQuantityOfProducts;
    private Long salesOfToday;
    private BigDecimal totalSalesRevenue;
    private String operatinTime;
    
    @JsonIgnore
    private ZonedDateTime createdAt;
    private Double growthFromStart;
    private Long employees; 
    
    // Hallazgo #2: Se añaden los tags al preview
    private List<TagDto> tags = new ArrayList<>();

    public StorePreviewDto(Long id, String name, String address, String status,
            Long totalProducts, Long currentQuantityOfProducts,
            Long salesOfToday, BigDecimal totalSalesRevenue,
            Long employees,
            ZonedDateTime createdAt,
            String timezone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.totalProducts = totalProducts != null ? totalProducts : 0L;
        this.currentQuantityOfProducts = currentQuantityOfProducts != null ? currentQuantityOfProducts : 0L;
        this.salesOfToday = salesOfToday != null ? salesOfToday : 0L;
        this.totalSalesRevenue = totalSalesRevenue != null ? totalSalesRevenue : BigDecimal.ZERO;
        this.employees = employees;
        this.createdAt = createdAt;
        this.timezone = timezone;
        this.growthFromStart = calculateDailyGrowth(createdAt);
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getCurrentQuantityOfProducts() {
        return currentQuantityOfProducts;
    }

    public void setCurrentQuantityOfProducts(Long currentQuantityOfProducts) {
        this.currentQuantityOfProducts = currentQuantityOfProducts;
    }

    private Double calculateDailyGrowth(ZonedDateTime createdAt) {
        if (createdAt == null || totalSalesRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        long daysAlive = Duration.between(createdAt, ZonedDateTime.now()).toDays();

        if (daysAlive <= 0)
            daysAlive = 1;

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

    public Long getEmployees() {
        return employees;
    }

    public void setEmployees(Long employees) {
        this.employees = employees;
    }

    public String getOperatinTime() {
        return operatinTime;
    }

    public void setOperatinTime(String operatinTime) {
        this.operatinTime = operatinTime;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }
}