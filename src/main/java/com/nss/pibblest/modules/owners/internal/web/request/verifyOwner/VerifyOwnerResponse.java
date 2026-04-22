package com.nss.pibblest.modules.owners.internal.web.request.verifyOwner;

import com.nss.pibblest.shared.responses.TranslatedResponse;

public class VerifyOwnerResponse extends TranslatedResponse {

    private String message;

    public VerifyOwnerResponse(String messageKey, Object... args) {
        super(messageKey, args);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
