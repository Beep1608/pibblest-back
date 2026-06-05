package com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActivateEmployeeRequest {
    
    @NotBlank(message = "{employee.validation.token.required}")
    private String token;

    @NotBlank(message = "{employee.validation.password.required}")
    @Size(min = 8, message = "{employee.validation.password.size}")
    private String newPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
