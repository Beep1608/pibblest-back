package com.nss.pibblest.modules.products.internal.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends TranslatedRuntimeException {

    public ProductNotFoundException() {
        super("product.not.found");
    }
}
