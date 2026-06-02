package com.nss.pibblest.modules.security.internal.core;

import java.util.Set;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.internal.core.exceptions.EmployeeBadCredentials;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerBadCredentials;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerNotExists;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerRepository;
import com.nss.pibblest.modules.security.internal.web.request.login.LoginRequest;
import com.nss.pibblest.modules.security.internal.web.request.login.LoginResponse;
import com.nss.pibblest.modules.tenant.SessionTrackerService;
import com.nss.pibblest.modules.tenant.TenantContext;
import com.nss.pibblest.shared.Permission;
import com.nss.pibblest.shared.Role;

@Service
public class SecurityService {

    private final OwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final SessionTrackerService sessionTrackerService;

    public SecurityService(OwnerRepository ownerRepository, EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, MessageSource messageSource,
            SessionTrackerService sessionTrackerService) {
        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.messageSource = messageSource;
        this.sessionTrackerService = sessionTrackerService;
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        if (request.getOrganizationCode() == null || request.getOrganizationCode().isBlank()) {
            return ResponseEntity.status(HttpStatus.OK).body(loginOwner(request));
        }
        return ResponseEntity.status(HttpStatus.OK).body(loginEmployee(request));
    }

    private LoginResponse loginOwner(LoginRequest request) {
        OwnerEntity ownerEntity = ownerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new OwnerBadCredentials("error.owner.bad.credentials", null));

        if (!passwordEncoder.matches(request.getPassword(), ownerEntity.getPassword())) {
            throw new OwnerBadCredentials("error.owner.bad.credentials", null);
        }

        String token = jwtService.generateToken(ownerEntity.getId(), ownerEntity.getName(), ownerEntity.getSchemaName(), true, Role.OWNER, Set.of(Permission.values()));

        String tokenId = jwtService.extractTokenId(token);
        sessionTrackerService.registerNewSession(ownerEntity.getId().toString(), tokenId);
       
        String message = messageSource.getMessage("owner.login.success", new Object[] { ownerEntity.getName() },
                LocaleContextHolder.getLocale());
        return new LoginResponse(message, token);
    }

    private LoginResponse loginEmployee(LoginRequest request) {
        OwnerEntity ownerEntity = ownerRepository
                .findEntityByOrganizationCode(request.getOrganizationCode())
                .orElseThrow(() -> new OwnerNotExists("error.organization.not.exists", request.getOrganizationCode()));

        String schema = ownerEntity.getSchemaName();
        TenantContext.setCurrentTenant(schema);

        EmployeeEntity employeeEntity = employeeRepository
                .findByUsername(request.getEmail())
                .orElseThrow(() -> new EmployeeBadCredentials("error.employee.bad.credentials", null));

        if (!passwordEncoder.matches(request.getPassword(), employeeEntity.getPassword())) {
            throw new EmployeeBadCredentials("error.employee.bad.credentials", null);
        }

        String token = jwtService.generateToken(employeeEntity.getId(), employeeEntity.getUsername(), ownerEntity.getSchemaName(), false, employeeEntity.getRole(), employeeEntity.getPermissions());

        String tokenId = jwtService.extractTokenId(token);
        sessionTrackerService.registerNewSession(employeeEntity.getUsername(), tokenId);

        String message = messageSource.getMessage("employee.login.success",
                new Object[] { employeeEntity.getUsername() }, LocaleContextHolder.getLocale());
        return new LoginResponse(message, token);
    }
}