package com.nss.pibblest.shared.web;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler (MessageSource messageSource){
        this.messageSource = messageSource;
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidationExceptions(MethodArgumentNotValidException ex){
        
            Map<String,String> errors = new HashMap<>();

            ex.getBindingResult().getFieldErrors().forEach(error -> {
                String fieldName = error.getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> handleDatabaseConstraints(DataIntegrityViolationException ex){

        Locale currentLocale = LocaleContextHolder.getLocale();


        String errorTitle = messageSource.getMessage("error.database.conflict.title", null,currentLocale);
        String errorMessage = messageSource.getMessage("error.database.conflict.message", null,currentLocale);

        Map<String,Object> response = new HashMap<>();

        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String,Object>> handlePropertyReferenceException(PropertyReferenceException ex){

        Locale locale = LocaleContextHolder.getLocale();
        String errorTitle = messageSource.getMessage("error.properties.reference.title",null, locale);

        String errorMessage = messageSource.getMessage("error.properties.reference.message", new Object[]{ ex.getPropertyName()},locale);

        Map<String,Object> response = new HashMap<>();

        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error",errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

}
