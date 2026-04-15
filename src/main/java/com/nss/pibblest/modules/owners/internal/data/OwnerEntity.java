package com.nss.pibblest.modules.owners.internal.data;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "owners", schema="identity")
public class OwnerEntity {

    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name="company", nullable=false, columnDefinition="TEXT")
    private String company;

    @Column(name="name", nullable=false, length=100)
    private String name;

    @Column(name="last_name", nullable=false, length=100)
    private String lastName;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String password;

    @Column(name = "organization_code", unique = true, length = 10)
    private String organizationCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    @Column(name = "schema_name", unique = true, length = 63)
    private String schemaName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    protected OwnerEntity() {}


    public OwnerEntity(String company, String name, String lastName, String email, String password, String organizationCode, String schemaName) {
        this.company = company;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.organizationCode = organizationCode;
        this.schemaName = schemaName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

     public String getCompany() {
        return company;
    }


    public void setCompany(String company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public ZonedDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(ZonedDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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
