package com.nss.pibblest.modules.security.internal.web.request.login;

import com.nss.pibblest.shared.responses.TranslatedResponse;

public class LoginResponse extends TranslatedResponse{
    
    public LoginResponse(String messageKey, Object... args) {
        super(messageKey, args);
    }
    
}
