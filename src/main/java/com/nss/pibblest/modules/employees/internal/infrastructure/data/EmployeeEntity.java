package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nss.pibblest.shared.Permission;
import com.nss.pibblest.shared.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="employees")
public class EmployeeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name="name", nullable=false, length=100)
    private String name;

    @Column(name="last_name", nullable=false, length=100)
    private String lastName;

    @Column(name="username", nullable=false, length=100)
    private String username;
    
    @Column(name="password", nullable=false, length=100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable=false)
    private Role role = Role.EMPLOYEE;

    // Nueva relación granular de permisos
    @OneToMany(mappedBy="employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeePermissionEntity> granularPermissions = new ArrayList<>();

    @OneToMany(mappedBy="employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeStoreEntity> employeeStores = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name="last_active_at")
    private ZonedDateTime lastActiveAt;

    @Column(name="deleted_at")
    private ZonedDateTime deletedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<EmployeePermissionEntity> getGranularPermissions() { return granularPermissions; }
    public void setGranularPermissions(List<EmployeePermissionEntity> granularPermissions) { this.granularPermissions = granularPermissions; }
    public List<EmployeeStoreEntity> getEmployeeStores() { return employeeStores; }
    public void setEmployeeStores(List<EmployeeStoreEntity> employeeStores) { this.employeeStores = employeeStores; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public ZonedDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(ZonedDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public ZonedDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(ZonedDateTime deletedAt) { this.deletedAt = deletedAt; }
}
