package com.nss.pibblest.modules.security.internal.web.request.login;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "{validation.login.email.notblank}")
    private String email;

    @NotBlank(message = "{validation.login.password.notblank}")
    private String password;

    private String organizationCode;

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
}
