package com.nss.pibblest.modules.owners.internal.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.owners.internal.core.OwnerService;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerResponse;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/owners")
@Tag(name="Owners", description="Endpoints para los owners")
public class OwnerController {
    
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping
    @Operation(
        summary="Registrar un owner",
        description="Registar un owner en identity"
    )
    @ApiResponses(
        {
            @ApiResponse(responseCode="201", description="Owner registrado exitosamente"),
            @ApiResponse(responseCode="400", description="Datos de entrada inválidos"),
            @ApiResponse(responseCode="409", description="Conflicto: El correo o la empresa ya están registrados"),
            @ApiResponse(responseCode="500", description="Error interno en el servidor")
        }
    )
    public ResponseEntity<CreateOwnerResponse> registerOwner(@Valid @RequestBody CreateOwnerRequest request){

        return ownerService.registerOwner(request);

    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyOwnerResponse> verifyOwner(@Valid @RequestBody VerifyOwnerRequest request){
        return ownerService.verifyOwner(request);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String,Object>> getMyProfile() {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

       Map<String,Object> response = new HashMap<>();

       response.put("email", authentication.getName());
       response.put("message", "El token es valido");

       return ResponseEntity.ok(response);
    }
    

    

}
