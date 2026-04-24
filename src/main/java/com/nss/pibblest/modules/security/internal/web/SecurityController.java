package com.nss.pibblest.modules.security.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.security.internal.core.SecurityService;
import com.nss.pibblest.modules.security.internal.web.request.login.LoginRequest;
import com.nss.pibblest.modules.security.internal.web.request.login.LoginResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/login")
@Tag(name="Auth", description="Endpoints para autenticación")
public class SecurityController {

    private final SecurityService securityService;

    public SecurityController(SecurityService securityService){

        this.securityService = securityService;

    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return securityService.login(request);
    }
    
}
