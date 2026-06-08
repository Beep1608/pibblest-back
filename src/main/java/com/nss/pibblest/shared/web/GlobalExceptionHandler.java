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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.nss.pibblest.modules.employees.internal.core.exceptions.EmployeeBadCredentials;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;
import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

import io.jsonwebtoken.ExpiredJwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseConstraints(DataIntegrityViolationException ex) {

        Locale currentLocale = LocaleContextHolder.getLocale();

        String errorTitle = messageSource.getMessage("error.database.conflict.title", null, currentLocale);
        String errorMessage = messageSource.getMessage("error.database.conflict.message", null, currentLocale);

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReferenceException(PropertyReferenceException ex) {

        Locale locale = LocaleContextHolder.getLocale();
        String errorTitle = messageSource.getMessage("error.properties.reference.title", null, locale);

        String errorMessage = messageSource.getMessage("error.properties.reference.message",
                new Object[] { ex.getPropertyName() }, locale);

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(ExpiredJwtException ex) {
        String error = messageSource.getMessage("error.jwt.expired", null, LocaleContextHolder.getLocale());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", error);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
        String errorTile = messageSource.getMessage("error.illegal.state", null, LocaleContextHolder.getLocale());

        String errorMessage = ex.getMessage();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNPROCESSABLE_CONTENT.value());
        response.put("error", errorTile);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        String errorTile = messageSource.getMessage("error.entity.not.found", null, LocaleContextHolder.getLocale());

        String errorMessage = ex.getMessage();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", errorTile);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(EmployeeBadCredentials.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeBadCredentials(EmployeeBadCredentials ex) {
        Locale currentLocale = LocaleContextHolder.getLocale();

        String errorTitle = messageSource.getMessage("error.auth.bad.credentials.title", null, currentLocale);
        String errorMessage = messageSource.getMessage(ex.getMessage(), ex.getArgs(), currentLocale);

        Map<String, Object> response = new HashMap<>();
        
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Hallazgo #2: Manejador base para cualquier TranslatedRuntimeException no atrapada de forma específica
    @ExceptionHandler(TranslatedRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleTranslatedRuntimeException(TranslatedRuntimeException ex) {
        Locale currentLocale = LocaleContextHolder.getLocale();

        // Título genérico para excepciones de negocio
        String errorTitle = messageSource.getMessage("error.business.rule.title", null, "Error de Operación", currentLocale);
        
        // Se resuelve el mensaje usando el key y los argumentos alojados en la excepción
        String errorMessage = messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey(), currentLocale);

        Map<String, Object> response = new HashMap<>();
        
        response.put("status", HttpStatus.UNPROCESSABLE_CONTENT.value());
        response.put("error", errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNPROCESSABLE_CONTENT.value());

        Throwable cause = ex.getCause();
        InvalidFormatException targetEx = null;

        while (cause != null) {
            if (cause instanceof InvalidFormatException) {
                targetEx = (InvalidFormatException) cause;
                break;
            }
            cause = cause.getCause(); // Seguimos bajando en la pila
        }

        if (targetEx != null) {

            String rejectedValue = targetEx.getValue() != null ? targetEx.getValue().toString() : "null";
            String fieldName = !targetEx.getPath().isEmpty() ? targetEx.getPath().get(0).getFieldName() : "desconocido";

            String errorTitle = messageSource.getMessage("error.invalid.data.title", null,
                    LocaleContextHolder.getLocale());
            String errorMessage = messageSource.getMessage("error.invalid.enum",
                    new Object[] { rejectedValue, fieldName }, LocaleContextHolder.getLocale());

            response.put("error", errorTitle);
            response.put("message", errorMessage);
            response.put("invalidValue", rejectedValue);
            response.put("field", fieldName);

            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
        }

        String causeSummary = ex.getCause() != null ? ex.getCause().getClass().getSimpleName() : "Request body missing";

        String errorTitle = messageSource.getMessage("error.http.not.readable", new Object[] { causeSummary },
                LocaleContextHolder.getLocale());
        String errorMessage = messageSource.getMessage("error.malformed.request", null,
                LocaleContextHolder.getLocale());

        response.put("error", errorTitle);
        response.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Acceso Denegado");
        // Nos dirá exactamente por qué falló (si fue nuestra validación o el @PreAuthorize)
        response.put("message", ex.getMessage()); 

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", org.springframework.http.HttpStatus.BAD_REQUEST.value());
        response.put("error", "Petición Incorrecta");
        response.put("message", ex.getMessage()); 

        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(response);
    }
}

