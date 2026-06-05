package com.nss.pibblest.modules.employees.internal.core;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.api.dto.EmployeeDto;
import com.nss.pibblest.modules.employees.internal.core.exceptions.EmployeeNotFound;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeePermissionEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeStoreEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.ModuleEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.ModuleRepository;
import com.nss.pibblest.modules.employees.internal.mappers.EmployeeMapper;
import com.nss.pibblest.modules.employees.internal.web.requests.ModulePermissionRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.StoreAssignmentRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.getAllEmployees.GetAllEmployeesResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeResponse;
import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final ModuleRepository moduleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final EmployeeMapper employeeMapper;
    private final JwtService jwtService;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%-";

    public EmployeeService(EmployeeRepository employeeRepository, StoreRepository storeRepository, 
                           ModuleRepository moduleRepository, PasswordEncoder passwordEncoder, 
                           MessageSource messageSource, EmployeeMapper employeeMapper, JwtService jwtService) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.moduleRepository = moduleRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.employeeMapper = employeeMapper;
        this.jwtService = jwtService;
    }

    @Transactional
    public ResponseEntity<CreateEmployeeResponse> createEmployee(CreateEmployeeRequest request) {
        // --- NUEVA VALIDACIÓN DE PERMISOS GRANULARES ---
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) {
            throw new org.springframework.security.access.AccessDeniedException("No autenticado");
        }

        boolean isOwner = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        // Si no es OWNER, es un empleado y debemos validar sus restricciones por tienda
        if (!isOwner && request.getStoreAssignments() != null) {
            for (StoreAssignmentRequest assignment : request.getStoreAssignments()) {
                Long storeId = assignment.storeId();

                // a. Validar que la tienda exista
                if (!storeRepository.existsById(storeId)) {
                    throw new EntityNotFoundException(messageSource.getMessage(
                        "error.store.not.found", new Object[]{storeId}, LocaleContextHolder.getLocale()));
                }

                // b. Validar que tenga permisos de CREAR empleados en ESA tienda específica
                String requiredAuthority = "STORE_" + storeId + "_MODULE_EMPLOYEES_CREATE";
                boolean hasStorePermission = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(requiredAuthority));

                if (!hasStorePermission) {
                    throw new org.springframework.security.access.AccessDeniedException(
                        messageSource.getMessage("error.employee.no.permission.for.store", new Object[]{storeId}, LocaleContextHolder.getLocale())
                    );
                }
            }
        }
        // --- FIN DE LA VALIDACIÓN ---

        // Lógica existente de creación...
        String baseUsername = generateBaseUserName(request.getName(), request.getLastName());
        String finalUsername = ensureUniqueUserName(baseUsername);
        
        EmployeeEntity employee = new EmployeeEntity();
        employee.setName(request.getName());
        employee.setLastName(request.getLastName());
        employee.setUsername(finalUsername);
        // Se coloca una contraseña temporal en lo que se activa la cuenta (Fase 2 - Opción 2)
        employee.setPassword(passwordEncoder.encode(generateRandomPassword(10))); 

        syncEmployeeAccess(employee, request.getStoreAssignments());

        EmployeeEntity savedEmployee = employeeRepository.save(employee);

        String activationToken = jwtService.generateActivationToken(savedEmployee.getId());
        String frontendUrl = "http://localhost:4200/auth/activate-account?token=" + activationToken;

        CreateEmployeeResponse response = new CreateEmployeeResponse(
            messageSource.getMessage("employee.created", new Object[]{savedEmployee.getUsername()}, LocaleContextHolder.getLocale()),
            frontendUrl
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Transactional
    public ResponseEntity<UpdateEmployeeResponse> editEmployee(UUID id, UpdateEmployeeRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        employee.setName(request.getName());
        employee.setLastName(request.getLastName());

        syncEmployeeAccess(employee, request.getStoreAssignments());

        employeeRepository.save(employee);

        return ResponseEntity.ok(new UpdateEmployeeResponse(
            messageSource.getMessage("employee.updated", null, locale)
        ));
    }

    public ResponseEntity<GetAllEmployeesResponse> getAllEmployees(Pageable pageable) {
        // Ignorar eliminados
        Page<EmployeeEntity> entities = employeeRepository.findByDeletedAtIsNull(pageable);
        Page<EmployeeDto> dtos = entities.map(employeeMapper::toDto);
        return ResponseEntity.ok(new GetAllEmployeesResponse(dtos));
    }

    public ResponseEntity<EmployeeDto> getEmployeeById(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));
        return ResponseEntity.ok(employeeMapper.toDto(employee));
    }

    @Transactional
    public ResponseEntity<Void> deleteEmployee(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        employee.setDeletedAt(ZonedDateTime.now());
        // Soft delete para las asociaciones de tienda también
        employee.getEmployeeStores().forEach(es -> es.setActive(false));
        
        employeeRepository.save(employee);
        
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<Void> activateEmployeeAccount(String token, String newPassword) {
        try {
            // 1. Extraemos el ID del token (si expiró o fue manipulado, lanzará excepción)
            UUID employeeId = jwtService.extractEmployeeIdFromActivationToken(token);

            // 2. Buscamos al empleado
            EmployeeEntity employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado"));

            // 3. Establecemos la contraseña real elegida por el usuario
            employee.setPassword(passwordEncoder.encode(newPassword));
            
            // Opcional: Podrías marcar lastActiveAt = ZonedDateTime.now() para saber que ya se activó
            
            employeeRepository.save(employee);
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            throw new IllegalArgumentException("El enlace de activación es inválido o ha expirado.");
        }
    }

    // NUEVO MÉTODO DE SINCRONIZACIÓN
    private void syncEmployeeAccess(EmployeeEntity employee, List<StoreAssignmentRequest> assignments) {
        if (assignments == null) assignments = new java.util.ArrayList<>();

        // Paso A: Extraer IDs de tiendas para sincronizar EmployeeStoreEntity
        Set<Long> requestedStoreIds = assignments.stream()
                .map(StoreAssignmentRequest::storeId)
                .collect(Collectors.toSet());

        employee.getEmployeeStores().removeIf(es -> !requestedStoreIds.contains(es.getStore().getId()));
        
        Set<Long> existingStoreIds = employee.getEmployeeStores().stream()
                .map(es -> es.getStore().getId())
                .collect(Collectors.toSet());

        for (Long storeId : requestedStoreIds) {
            if (!existingStoreIds.contains(storeId)) {
                StoreEntity store = storeRepository.findById(storeId).orElseThrow();
                employee.getEmployeeStores().add(new EmployeeStoreEntity(store, employee, true));
            }
        }

        // Paso B: Algoritmo "Diffing" para Permisos Granulares
        Set<String> requestedPermKeys = new HashSet<>();
        for (StoreAssignmentRequest sa : assignments) {
            for (ModulePermissionRequest mp : sa.permissions()) {
                for (String action : mp.actions()) {
                    // Generamos llaves únicas: Ej "1_2_READ" (storeId_moduleId_action)
                    requestedPermKeys.add(sa.storeId() + "_" + mp.moduleId() + "_" + action.toUpperCase());
                }
            }
        }

        // 1. Eliminar permisos que ya no están en el request del frontend
        employee.getGranularPermissions().removeIf(p -> {
            String key = p.getStore().getId() + "_" + p.getModule().getId() + "_" + p.getAction();
            return !requestedPermKeys.contains(key);
        });

        // 2. Detectar permisos que ya existen para no insertarlos doble
        Set<String> existingPermKeys = employee.getGranularPermissions().stream()
                .map(p -> p.getStore().getId() + "_" + p.getModule().getId() + "_" + p.getAction())
                .collect(Collectors.toSet());

        // 3. Añadir los permisos nuevos
        for (StoreAssignmentRequest sa : assignments) {
            // Usamos getReferenceById para no golpear la BD extra, solo necesitamos el Proxy para la llave foránea
            StoreEntity storeProxy = storeRepository.getReferenceById(sa.storeId());
            
            for (ModulePermissionRequest mp : sa.permissions()) {
                ModuleEntity moduleProxy = moduleRepository.getReferenceById(mp.moduleId());
                
                for (String action : mp.actions()) {
                    String key = sa.storeId() + "_" + mp.moduleId() + "_" + action.toUpperCase();
                    
                    if (!existingPermKeys.contains(key)) {
                        EmployeePermissionEntity perm = new EmployeePermissionEntity();
                        perm.setEmployee(employee);
                        perm.setStore(storeProxy);
                        perm.setModule(moduleProxy);
                        perm.setAction(action.toUpperCase());
                        employee.getGranularPermissions().add(perm);
                    }
                }
            }
        }
    }

    private String generateBaseUserName(String name, String lastName){
        String firstName = name.trim().split("\\s+")[0];
        String firstLastName = lastName.trim().split("\\s+")[0];
        String rawUserName = firstName.substring(0,1) + firstLastName;
        return stripAccents(rawUserName).toLowerCase();
    }

    private String ensureUniqueUserName(String baseUsername){
        String username = baseUsername;
        int counter = 1;
        // Ignorar eliminados al verificar unicidad
        while(employeeRepository.existsByUsernameAndDeletedAtIsNull(username)){
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    private String stripAccents(String str){
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return str.replaceAll("[^a-zA-Z0-9]","");
    }

    private String generateRandomPassword(int length){
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i< length; i++){
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
