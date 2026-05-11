package com.nss.pibblest.modules.sales.internal.infrastructure.data;

import java.math.BigDecimal;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="sales_details")
public class SaleDetailEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sale_id", nullable=false)
    private SaleEntity sale;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="product_id", nullable=false)
    private ProductEntity product;

    @Column(nullable=false)
    private Integer quantity;

    @Column(name="unit_price", nullable= false, precision=12, scale=2)
    private BigDecimal unitPrice;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal subtotal;

    public SaleDetailEntity(SaleEntity sale, ProductEntity product, Integer quantity, BigDecimal unitPrice) {
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
       this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public SaleDetailEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SaleEntity getSale() {
        return sale;
    }

    public void setSale(SaleEntity sale) {
        this.sale = sale;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    
}
