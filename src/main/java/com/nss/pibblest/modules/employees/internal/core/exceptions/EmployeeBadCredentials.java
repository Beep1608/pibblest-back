package com.nss.pibblest.modules.employees.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class EmployeeBadCredentials extends TranslatedRuntimeException{
    
    public EmployeeBadCredentials(String messageKey, Object... args) {
        super(messageKey, args);
    }
    
}
