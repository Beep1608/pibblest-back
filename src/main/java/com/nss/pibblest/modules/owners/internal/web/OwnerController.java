package com.nss.pibblest.modules.owners.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.owners.internal.core.OwnerService;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.OwnerProfileResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.UpdateProfileRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenResponse;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/owners")
@Tag(name="Owners", description="Endpoints para la gestión de Owners")
public class OwnerController {
    
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping("/register")
    @Operation(summary="Registrar un owner")
    public ResponseEntity<CreateOwnerResponse> registerOwner(@Valid @RequestBody CreateOwnerRequest request){
        return ownerService.registerOwner(request);
    }

    @PostMapping("/verify")
    @Operation(summary="Verificar cuenta de Owner")
    public ResponseEntity<VerifyOwnerResponse> verifyOwner(@Valid @RequestBody VerifyOwnerRequest request){
        return ownerService.verifyOwner(request);
    }

    @PostMapping("/resend-token")
    @Operation(summary="Reenviar token de verificación")
    public ResponseEntity<ResendTokenResponse> resendToken(@Valid @RequestBody ResendTokenRequest request){
        return ownerService.resendToken(request);
    }

    @GetMapping("/profile")
    @Operation(
        summary="Obtener perfil", 
        description="Retorna el perfil completo del Owner autenticado basado en su JWT."
    )
    @ApiResponse(responseCode="200", description="Perfil obtenido exitosamente")
    public ResponseEntity<OwnerProfileResponse> getMyProfile() {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       // El nombre del usuario en el token suele ser el email en configuraciones estándar
       return ownerService.getProfile(authentication.getName());
    }

    @PutMapping("/profile")
    @Operation(
        summary="Actualizar perfil", 
        description="Actualiza la información básica (nombre y apellido) del Owner autenticado."
    )
    @ApiResponses({
        @ApiResponse(responseCode="200", description="Perfil actualizado exitosamente"),
        @ApiResponse(responseCode="400", description="Datos de validación incorrectos")
    })
    public ResponseEntity<OwnerProfileResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       return ownerService.updateProfile(authentication.getName(), request);
    }
}
