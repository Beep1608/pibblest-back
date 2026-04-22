package com.nss.pibblest.modules.security.internal.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.security.internal.core.SecurityService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/login")
@Tag(name="Auth", description="Endpoints para autenticación")
public class SecurityController {

    private final SecurityService securityService;

    public SecurityController(SecurityService securityService){

        this.securityService = securityService;

    }

    //public ResponseEntity<LoginOwnerResponse> loginOwner(LoginOwnerRequest request){
    //    return securityService.loginOwner(request);
    //}
    
}
