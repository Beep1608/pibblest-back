package com.nss.pibblest.modules.owners.internal.web.request.createOwner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateOwnerRequest {

    @NotBlank(message="{validation.owner.company.notblank}")
    @Size(max = 100, message = "{validation.owner.company.size}")
    @Schema(description="Nombre de la empresa del propietario", example="Minion Inc.")
    private String company;

    @NotBlank(message = "{validation.owner.name.notblank}")
    @Size(max = 100, message = "{validation.owner.name.size}")
    @Schema(description="Nombre del propietario", example="Papoi")
    private String name;

    @NotBlank(message = "{validation.owner.lastname.notblank}")
    @Size(max = 100, message = "{validation.owner.lastname.size}")
    @Schema(description="Apellido del propietario", example="Morales")
    private String lastName;

    @Email(message="{validation.owner.email.format}") 
    @NotBlank(message = "{validation.owner.email.notblank}")
    @Size(max = 255, message = "{validation.owner.email.size}")
    @Schema(description="Email del propietario", example="hola@example.com")
    private String email;

    @NotBlank(message = "{validation.owner.password.notblank}") 
    @Size(min = 8, max = 64, message = "{validation.owner.password.size}") 
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).*$", message = "{validation.owner.password.complexity}")
    @Schema(description="Password de la cuenta", example="Secreta123!")
    private String password;

    public CreateOwnerRequest() {}

    public CreateOwnerRequest(String company){
        this.company = company;
    }

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
    
    @Override
    public String toString() {
        return "CreateOwnerRequest{" +
                "company='" + company + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" + 
                '}';
    }
}