package com.nss.pibblest.modules.owners.internal.core.exceptions;

import com.nss.pibblest.shared.internal.exceptions.TranslatedRuntimeException;

public class OwnerAlreadyExists extends TranslatedRuntimeException {

    public OwnerAlreadyExists(String message, Object ... args){
        super(message, args);
    }
    
}
