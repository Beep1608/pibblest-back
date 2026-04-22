package com.nss.pibblest.modules.owners.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class OwnerAlreadyVerified extends TranslatedRuntimeException {
    
    public OwnerAlreadyVerified(String messageKey, Object... args) {
        super(messageKey, args);
    }
    
}
