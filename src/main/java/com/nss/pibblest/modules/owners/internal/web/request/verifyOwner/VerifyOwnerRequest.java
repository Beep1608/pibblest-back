package com.nss.pibblest.modules.owners.internal.web.request.verifyOwner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyOwnerRequest {

    @NotBlank(message = "{validation.owner.token.notblank}")
    @Size(min = 10, max = 512, message = "{validation.owner.token.size}")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
