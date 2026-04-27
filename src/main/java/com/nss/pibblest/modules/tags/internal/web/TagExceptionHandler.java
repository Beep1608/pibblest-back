package com.nss.pibblest.modules.tags.internal.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nss.pibblest.modules.tags.internal.core.exceptions.TagsNotFound;

@RestControllerAdvice(basePackages={"com.nss.pibblest.modules.tags.internal.web"})
public class TagExceptionHandler {


    @ExceptionHandler(TagsNotFound.class)
    public ResponseEntity<Map<String, Object>> handleTagsNotFound(TagsNotFound ex){

        Map<String, Object> response = new HashMap<>();

        response.put("status",HttpStatus.NOT_FOUND.value());
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
}

