package com.nss.pibblest.modules.security.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class OneTimeTokenInvalid extends TranslatedRuntimeException {

    public OneTimeTokenInvalid(String messageKey, Object ... args) {
        super(messageKey, args);
     
    }
    
}
