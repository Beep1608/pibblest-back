package com.nss.pibblest.modules.owners.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class OwnerNotExists extends TranslatedRuntimeException {

    public OwnerNotExists(String messageKey, Object ... args) {
        super(messageKey, args);
        //TODO Auto-generated constructor stub
    }
    
}
