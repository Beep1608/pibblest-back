package com.nss.pibblest.modules.stores.internal.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nss.pibblest.modules.stores.internal.core.exceptions.StoreNotFound;

@RestControllerAdvice(basePackages={"com.nss.pibblest.modules.stores.internal.web","com.nss.pibblest.modules.tags.internal.web"})
public class StoreExceptionHandler {



    @ExceptionHandler(StoreNotFound.class)
    public ResponseEntity<Map<String, Object>> handleStoreNotFound(StoreNotFound ex){
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

    }
}
