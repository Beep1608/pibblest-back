package com.nss.pibblest.modules.owners.internal.web.request.createOwner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class CreateOwnerRequest {

    @NotBlank 
    @Schema(description="Nombre de la empresa del propietario", example="Minion Inc.")
    private String company;

    @NotBlank 
    @Schema(description="Nombre del propietario", example="Papoi")
    private String name;

    @NotBlank 
    @Schema(description="Apellido del propietario", example="Morales")
    private String lastName;

    @Email @NotBlank 
    @Schema(description="Email del propietario", example="hola@example.com")
    private String email;

    @NotBlank @Size(min = 8) 
    @Schema(description="Password de la cuenta", example="hola@example.com")
    private String password;

    private String organizationCode;

    private String schemaName;

    // Constructor vacío (necesario para frameworks como Jackson/Spring)
    public CreateOwnerRequest() {}

    public CreateOwnerRequest(String company){
        this.company = company;
        
    }

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
    
    @Override
    public String toString() {
        return "CreateOwnerRequest{" +
                "company='" + company + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" + // ¡Contraseña enmascarada!
                ", organizationCode='" + organizationCode + '\'' +
                ", schemaName='" + schemaName + '\'' +
                '}';
    }
}