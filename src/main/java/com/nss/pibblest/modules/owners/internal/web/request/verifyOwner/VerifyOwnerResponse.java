package com.nss.pibblest.modules.owners.internal.web.request.verifyOwner;


public class VerifyOwnerResponse  {

    private String message;

    public VerifyOwnerResponse(String message, Object... args) {

        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
