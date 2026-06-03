package com.nss.pibblest.modules.security.internal.core;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.internal.core.exceptions.EmployeeBadCredentials;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerNotExists;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerRepository;
import com.nss.pibblest.modules.security.internal.web.request.login.LoginRequest;
import com.nss.pibblest.modules.security.internal.web.request.login.LoginResponse;
import com.nss.pibblest.modules.tenant.SessionTrackerService;
import com.nss.pibblest.modules.tenant.TenantContext;

@Service
public class SecurityService {

    private final OwnerRepository ownerRepository;
    private final TenantAuthenticationHelper tenantAuthHelper; // Inyectamos el nuevo helper
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final SessionTrackerService sessionTrackerService;

    public SecurityService(OwnerRepository ownerRepository, TenantAuthenticationHelper tenantAuthHelper,
            PasswordEncoder passwordEncoder, JwtService jwtService, MessageSource messageSource,
            SessionTrackerService sessionTrackerService) {
        this.ownerRepository = ownerRepository;
        this.tenantAuthHelper = tenantAuthHelper; // Reemplaza al employeeRepository directo
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.messageSource = messageSource;
        this.sessionTrackerService = sessionTrackerService;
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(processUnifiedLogin(request));
    }

    private LoginResponse processUnifiedLogin(LoginRequest request) {
        OwnerEntity ownerEntity;

        // PASO 1: Descubrimiento de Esquema (Directorio Global)
        if (request.getOrganizationCode() != null && !request.getOrganizationCode().isBlank()) {
            ownerEntity = ownerRepository.findEntityByOrganizationCode(request.getOrganizationCode())
                    .orElseThrow(() -> new OwnerNotExists("error.organization.not.exists", request.getOrganizationCode()));
        } else {
            // Si no manda código, asumimos que es Owner y buscamos su esquema por email
            ownerEntity = ownerRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EmployeeBadCredentials("error.bad.credentials", null));
        }

        String schema = ownerEntity.getSchemaName();
        String orgCode = ownerEntity.getOrganizationCode(); 
        
        // PASO 2: Establecer contexto del Inquilino
        TenantContext.setCurrentTenant(schema);

        try {
            // PASO 3: Autenticación en la tabla employees (Forzando nueva conexión)
            EmployeeEntity employeeEntity = tenantAuthHelper.findEmployeeByUsername(request.getEmail())
                    .orElseThrow(() -> new EmployeeBadCredentials("error.employee.bad.credentials", null));

            if (!passwordEncoder.matches(request.getPassword(), employeeEntity.getPassword())) {
                throw new EmployeeBadCredentials("error.employee.bad.credentials", null);
            }

            // Identificamos si es dueño basándonos en el rol que tiene dentro de la tabla employees
            boolean isOwner = employeeEntity.getRole().name().equalsIgnoreCase("OWNER");

            // PASO 4: Generación de Tokens e inyección de contexto
            String token = jwtService.generateToken(
                    employeeEntity.getId(), 
                    employeeEntity.getUsername(), 
                    schema, 
                    isOwner, 
                    employeeEntity.getRole(), 
                    employeeEntity.getPermissions(), 
                    orgCode
            );

            String tokenId = jwtService.extractTokenId(token);
            
            // La sesión siempre se registra como orgCode + UUID de la tabla employees
            String sessionKey = (orgCode != null ? orgCode : "") + employeeEntity.getId().toString();
            sessionTrackerService.registerNewSession(sessionKey, tokenId);

            String message = messageSource.getMessage("login.success.unified",
                    new Object[] { employeeEntity.getUsername() }, LocaleContextHolder.getLocale());
            
            return new LoginResponse(message, token);
            
        } finally {
            // Siempre limpiamos el contexto para evitar Tenant Leaks
            TenantContext.clear();
        }
    }
}