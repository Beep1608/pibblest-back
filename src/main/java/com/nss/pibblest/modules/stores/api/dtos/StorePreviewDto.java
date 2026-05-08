package com.nss.pibblest.modules.stores.api.dtos;

public record StorePreviewDto (
    Long id,
    String name,
    String address,
    String status,
    float growthFromStart, //de la tabla sales calcular el crecimiento desde la fecha en que fue creada la tienda
    int employees, // de la tabla stores_employees
    int salesOfToday,  // de la tabla sales que coincidan con la fecha de hoy
    int totalProduct, // de la tabla stores_products (cantidad de registros asociados a la tieneda pero que no esten desactivados)
    int currentQuantityOfProducts, // la sumatoria de la columna quantity de la tabla stores_products que esten activos
    String createdAt
){

}