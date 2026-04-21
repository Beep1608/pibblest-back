package com.nss.pibblest.modules.owners.internal.web.request.verifyOwner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class VerifyOwnerRequest {

    @NotBlank
    @NotEmpty
    private  String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    

    
}
