package com.nss.pibblest.modules.security.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class OneTimeTokenExpired extends TranslatedRuntimeException {
    
    public OneTimeTokenExpired(String messageKey, Object... args) {
        super(messageKey, args);
    }
    
}
