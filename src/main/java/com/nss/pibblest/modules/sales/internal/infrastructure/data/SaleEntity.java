package com.nss.pibblest.modules.sales.internal.infrastructure.data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales")
public class SaleEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="store_id", nullable=false)
    private StoreEntity store;

    @Column(name="total_amount", nullable=false, precision=12, scale =2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable=false, length=50)
    private String status = "COMPLETED";

    @Column(name="created_at", updatable=false)
    private ZonedDateTime createdAt;    

    @Column(name="updated_at")
    private ZonedDateTime updatedTime;

    @Column(name="deleted_at")
    private ZonedDateTime deletedAt;

    @OneToMany(mappedBy="sale", cascade= CascadeType.ALL, orphanRemoval=true)
    private List<SaleDetailEntity> details = new ArrayList<>();

    public SaleEntity(Long id, StoreEntity store, BigDecimal totalAmount, String status, ZonedDateTime createdAt,
            ZonedDateTime updatedTime) {
        this.id = id;
        this.store = store;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedTime = updatedTime;
    }

    public SaleEntity() {
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt = ZonedDateTime.now();
        this.updatedTime = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedTime = ZonedDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StoreEntity getStore() {
        return store;
    }

    public void setStore(StoreEntity store) {
        this.store = store;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(ZonedDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public List<SaleDetailEntity> getDetails() {
        return details;
    }

    public void setDetails(List<SaleDetailEntity> details) {
        this.details = details;
    }

    public ZonedDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(ZonedDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void addDetail(SaleDetailEntity detail){
        details.add(detail);
        detail.setSale(this);
    }

    public void removeDetail(SaleDetailEntity detail){
        details.remove(detail);
        detail.setSale(null);
    }

    
    
}
