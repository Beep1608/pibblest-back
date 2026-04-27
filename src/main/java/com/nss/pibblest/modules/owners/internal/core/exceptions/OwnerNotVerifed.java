package com.nss.pibblest.modules.owners.internal.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class OwnerNotVerifed extends TranslatedRuntimeException {

    public OwnerNotVerifed(String messageKey, Object[] args) {
        super(messageKey, args);
        //TODO Auto-generated constructor stub
    }
    
}
