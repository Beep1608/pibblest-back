package com.nss.pibblest.modules.owners.internal.web.request.resendToken;

public class ResendTokenResponse {

    private String message;

    public ResendTokenResponse(String message){
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    
    
}
