package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;

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
@Table(name="employees_stores")
public class EmployeeStoreEntity {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="store_id", nullable=false)
    private StoreEntity store;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="employee_id", nullable=false)
    private EmployeeEntity employee;

    @Column(name="is_active")
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;


    public EmployeeStoreEntity() {
    }


    public EmployeeStoreEntity(StoreEntity store, EmployeeEntity employee, boolean isActive) {
        this.store = store;
        this.employee = employee;
        this.isActive = isActive;
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


    public EmployeeEntity getEmployee() {
        return employee;
    }


    public void setEmployee(EmployeeEntity employee) {
        this.employee = employee;
    }


    public boolean isActive() {
        return isActive;
    }


    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }


    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
