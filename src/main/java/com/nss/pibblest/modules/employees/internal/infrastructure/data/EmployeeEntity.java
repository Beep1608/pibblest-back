package com.nss.pibblest.modules.employees.internal.infrastructure.data;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.nss.pibblest.shared.enums.Permission;
import com.nss.pibblest.shared.enums.Role;

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
import jakarta.persistence.Table;

@Entity
@Table(name="employees")
public class EmployeeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name="username", nullable=false, length=100)
    private String username;
    
    @Column(name="password", nullable=false, length=100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable=false)
    private Role role = Role.EMPLOYEE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="employee_permissions", joinColumns=@JoinColumn(name="employee_id"))
    @Enumerated(EnumType.STRING)
    @Column(name="permission")
    private Set<Permission> permissions = new HashSet<>();

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
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public ZonedDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(ZonedDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public ZonedDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(ZonedDateTime deletedAt) { this.deletedAt = deletedAt; }
}
