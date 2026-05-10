package com.nss.pibblest.modules.stores.api.dtos;

import java.time.ZonedDateTime;

public class StorePreviewDto {

    private Long id;
    private String name;
    private String address;
    private String status;
    // float growthFromStart, //de la tabla sales calcular el crecimiento desde la
    // fecha en que fue creada la tienda
    // int employees, // de la tabla stores_employees
    // int salesOfToday, // de la tabla sales que coincidan con la fecha de hoy
    private Long totalProduct; // de la tabla stores_products (cantidad de registros asociados a la tieneda
                       // pero que no esten desactivados)
    private Long currentQuantityOfProducts; // la sumatoria de la columna quantity de la tabla stores_products que esten
                                 // activos
    private ZonedDateTime createdAt;


    public StorePreviewDto(Long id, String name, String address, String status, Long totalProduct,
            Long currentQuantityOfProducts, ZonedDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.totalProduct = totalProduct;
        this.currentQuantityOfProducts = currentQuantityOfProducts;
        this.createdAt = createdAt;
    }


    public StorePreviewDto(){}


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


}