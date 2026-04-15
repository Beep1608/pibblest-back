package com.nss.pibblest.modules.owners.internal.web.request.createOwner;

import jakarta.validation.constraints.*;
import java.util.UUID;
import java.text.Normalizer;

public class CreateOwnerRequest {

    @NotBlank 
    private String company;

    @NotBlank 
    private String name;

    @NotBlank 
    private String lastName;

    @Email @NotBlank 
    private String email;

    @NotBlank @Size(min = 8) 
    private String password;

    @Size(max = 10) 
    private String organizationCode;

    @Size(max = 63) 
    private String schemaName;

    // Constructor vacío (necesario para frameworks como Jackson/Spring)
    public CreateOwnerRequest() {}

    /**
     * Método para inicializar los valores del backend basados en la compañía.
     * Puedes llamar a esto desde tu Service.
     */

    // --- Getters y Setters ---

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOrganizationCode() { return organizationCode; }
    public void setOrganizationCode(String organizationCode) { this.organizationCode = organizationCode; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }
}