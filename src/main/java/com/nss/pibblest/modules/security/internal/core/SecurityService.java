package com.nss.pibblest.modules.security.internal.core;

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
import com.nss.pibblest.modules.tenant.TenantContext;

@Service
public class SecurityService {

    private final OwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public SecurityService(OwnerRepository ownerRepository, EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request) {

        if (request.getOrganizationCode() == null || request.getOrganizationCode().isBlank()) {

            ResponseEntity<LoginResponse> response = ResponseEntity.status(HttpStatus.OK).body(loginOwner(request));
            return response;
        }

        ResponseEntity<LoginResponse> response = ResponseEntity.status(HttpStatus.OK).body(loginEmployee(request));
        return response;
    }

    private LoginResponse loginOwner(LoginRequest request) {
        System.out.println("NO EXISTE EL OWNER :");
        OwnerEntity ownerEntity = ownerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new OwnerBadCredentials("error.owner.bad.credentials", null));

        System.out.println("SI EXISTE EL OWNER : "+ownerEntity.getEmail());
        if (!passwordEncoder.matches(request.getPassword(), request.getPassword())) {
            throw new OwnerBadCredentials("error.owner.bad.credentials", null);
        }

        String token = jwtService.generateToken(ownerEntity.getId(), ownerEntity.getName(), ownerEntity.getCompany());

        return new LoginResponse("owner.login.success", ownerEntity.getName(), token);
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

        if (passwordEncoder.matches(request.getPassword(), employeeEntity.getPassword())) {
            throw new EmployeeBadCredentials("error.employee.bad.credentials", null);
        }

        String token = jwtService.generateToken(employeeEntity.getId(), employeeEntity.getUsername(),
                ownerEntity.getCompany());
        return new LoginResponse("employee.login.success", employeeEntity.getUsername(), token);

    }

}