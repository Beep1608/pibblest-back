package com.nss.pibblest.modules.security.internal.web;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerBadCredentials;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenExpired;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenInvalid;

@RestControllerAdvice(basePackages = {
        "com.nss.pibblest.modules.owners.internal.web",
        "com.nss.pibblest.modules.security.internal.web"
})
public class SecurityExceptionHandler {

    private final MessageSource messageSource;

    public SecurityExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(OneTimeTokenExpired.class)
    public ResponseEntity<Map<String, Object>> handleOneTimeTokenExpired(OneTimeTokenExpired ex) {

        Locale currentLocale = LocaleContextHolder.getLocale();
        String errorTitle = messageSource.getMessage("error.one.time.token.expired.title", null, currentLocale);

        String localizedMessage = messageSource.getMessage(
                ex.getMessage(),
                ex.getArgs(),
                LocaleContextHolder.getLocale());

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", errorTitle);
        response.put("message", localizedMessage);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

    }

    @ExceptionHandler(OneTimeTokenInvalid.class)
    public ResponseEntity<Map<String, Object>> handleOneTimeTokenInvalid(OneTimeTokenInvalid ex) {

        Locale currentLocale = LocaleContextHolder.getLocale();
        String errorTitle = messageSource.getMessage("error.one.time.token.invalid.title", null, currentLocale);

        String localizedMessage = messageSource.getMessage(
                ex.getMessage(),
                ex.getArgs(),
                currentLocale);

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", errorTitle);
        response.put("message", localizedMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }


    @ExceptionHandler(OwnerBadCredentials.class)
    public ResponseEntity<Map<String, Object>> handleOwnerBadCredentials (OwnerBadCredentials ex){
        Locale currentLocale = LocaleContextHolder.getLocale();

        String errorTitle = messageSource.getMessage("error.owner.bad.credentials", null,currentLocale);

        String localizeMessage = messageSource.getMessage(
            ex.getMessage(), 
            ex.getArgs(),
            currentLocale);

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", errorTitle);
        response.put("message", localizeMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
