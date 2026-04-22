package com.nss.pibblest.modules.owners.internal.web;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyExists;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerNotExists;

@RestControllerAdvice(basePackages="com.nss.pibblest.modules.owners.internal.web")
public class OwnerExceptionHandler {
    
    private final MessageSource messageSource;

    public OwnerExceptionHandler(MessageSource messageSource){
        this.messageSource = messageSource;
    }

    @ExceptionHandler(OwnerAlreadyExists.class)
    public ResponseEntity<Map<String,Object>> handleOwnerAlreadyExists(OwnerAlreadyExists ex){
        
        Locale currentLocale = LocaleContextHolder.getLocale();

        String errorTitle = messageSource.getMessage("error.owner.conflict.title", null,currentLocale);

        String localizedMessage = messageSource.getMessage(
            ex.getMessage(),
            ex.getArgs(), 
            LocaleContextHolder.getLocale()
        );

        Map<String,Object> response = new HashMap<>();
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", errorTitle);
        response.put("message", localizedMessage);
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OwnerNotExists.class)
    public ResponseEntity<Map<String, Object>> handleOwnerNotExists (OwnerNotExists ex){
        Locale currentLocale = LocaleContextHolder.getLocale();

        String errorTitle = messageSource.getMessage("error.owner.not.exits.title", null, currentLocale);

        String localizedMessage = messageSource.getMessage( ex.getMessage(),
            ex.getArgs(), 
            LocaleContextHolder.getLocale()
        );

        Map<String,Object> response = new HashMap<>();
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", errorTitle);
        response.put("message", localizedMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

}
