package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.time.ZonedDateTime;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name="employee_permissions")
public class EmployeePermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="employee_id", nullable=false)
    private EmployeeEntity employee;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="store_id", nullable=false)
    private StoreEntity store;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="module_id", nullable=false)
    private ModuleEntity module;

    @Column(name="action", nullable=false)
    private String action; // Ej: CREATE, READ, UPDATE, DELETE

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EmployeeEntity getEmployee() { return employee; }
    public void setEmployee(EmployeeEntity employee) { this.employee = employee; }
    public StoreEntity getStore() { return store; }
    public void setStore(StoreEntity store) { this.store = store; }
    public ModuleEntity getModule() { return module; }
    public void setModule(ModuleEntity module) { this.module = module; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}
