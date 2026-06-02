package com.nss.pibblest.modules.sales.internal.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SaleValidationException extends TranslatedRuntimeException {
    public SaleValidationException(String message) {
        super(message);
    }
}
