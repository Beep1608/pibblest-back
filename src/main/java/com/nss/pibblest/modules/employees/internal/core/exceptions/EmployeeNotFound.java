package com.nss.pibblest.modules.employees.internal.core.exceptions;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

public class EmployeeNotFound extends TranslatedRuntimeException{
    
    public EmployeeNotFound(String messageKey, Object... args) {
        super(messageKey, args);
    }
    
}
