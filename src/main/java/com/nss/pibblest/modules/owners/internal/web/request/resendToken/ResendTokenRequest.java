package com.nss.pibblest.modules.owners.internal.web.request.resendToken;

import jakarta.validation.constraints.NotNull;

public class ResendTokenRequest {
    
    @NotNull(message="{validation.owner.email.notblank}")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
