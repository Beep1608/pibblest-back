package com.nss.pibblest.modules.owners.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class OwnerBadCredentials extends TranslatedRuntimeException {
    
    public OwnerBadCredentials(String messageKey, Object... args) {
        super(messageKey, args);
    }
    
}
