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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.ActivateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.ResendActivationResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeResponse;
import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.tenant.SessionTrackerService;
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
    private final SessionTrackerService sessionTrackerService;
    private final jakarta.persistence.EntityManagerFactory entityManagerFactory;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%-";

    public EmployeeService(EmployeeRepository employeeRepository, StoreRepository storeRepository, 
                           ModuleRepository moduleRepository, PasswordEncoder passwordEncoder, 
                           MessageSource messageSource, EmployeeMapper employeeMapper, 
                           JwtService jwtService, SessionTrackerService sessionTrackerService,
                           jakarta.persistence.EntityManagerFactory entityManagerFactory) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.moduleRepository = moduleRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.employeeMapper = employeeMapper;
        this.jwtService = jwtService;
        this.sessionTrackerService = sessionTrackerService;
        this.entityManagerFactory = entityManagerFactory;
    }

    // HELPER: Verifica que el usuario tenga permisos sobre TODAS las tiendas a las que pertenece el empleado objetivo
    private void checkTargetStoresPermission(Authentication auth, EmployeeEntity targetEmployee, String action) {
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        if (isOwner) return;

        List<EmployeeStoreEntity> activeStores = targetEmployee.getEmployeeStores().stream()
                .filter(EmployeeStoreEntity::isActive).toList();

        if (activeStores.isEmpty()) {
            throw new AccessDeniedException("Solo un propietario puede modificar a empleados sin sucursal asignada.");
        }

        for (EmployeeStoreEntity es : activeStores) {
            Long storeId = es.getStore().getId();
            String requiredAuth = "STORE_" + storeId + "_MODULE_EMPLOYEES_" + action.toUpperCase();
            boolean hasPerm = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(requiredAuth));
            if (!hasPerm) {
                throw new AccessDeniedException(messageSource.getMessage("error.employee.no.permission.for.store", new Object[]{storeId}, LocaleContextHolder.getLocale()));
            }
        }
    }

    // HELPER: Bloquea bypasses por listas vacías y evita escalación de privilegios en el payload
    private void validateStoreAndPermissionAssignments(Authentication auth, EmployeeEntity authEmployee, List<StoreAssignmentRequest> assignments, String requestedAction) {
        Locale locale = LocaleContextHolder.getLocale();
        
        if (assignments == null || assignments.isEmpty()) {
            throw new AccessDeniedException("Debes asignar obligatoriamente al menos una sucursal al empleado.");
        }

        for (StoreAssignmentRequest assignment : assignments) {
            Long storeId = assignment.storeId();

            if (!storeRepository.existsById(storeId)) {
                throw new EntityNotFoundException(messageSource.getMessage("error.store.not.found", new Object[]{storeId}, locale));
            }

            boolean isAssignedToStore = authEmployee.getEmployeeStores().stream()
                    .anyMatch(es -> es.getStore().getId().equals(storeId) && es.isActive());
            if (!isAssignedToStore) {
                throw new AccessDeniedException(messageSource.getMessage("error.employee.not.assigned.to.store", new Object[]{storeId}, locale));
            }

            String requiredEmpAuthority = "STORE_" + storeId + "_MODULE_EMPLOYEES_" + requestedAction.toUpperCase();
            boolean hasEmpPermission = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(requiredEmpAuthority));
            if (!hasEmpPermission) {
                throw new AccessDeniedException(messageSource.getMessage("error.employee.no.permission.for.store", new Object[]{storeId}, locale));
            }

            if (assignment.permissions() != null) {
                for (ModulePermissionRequest mp : assignment.permissions()) {
                    ModuleEntity module = moduleRepository.findById(mp.moduleId())
                            .orElseThrow(() -> new EntityNotFoundException("Módulo especificado no encontrado"));

                    if (mp.actions() != null) {
                        for (String action : mp.actions()) {
                            String requiredGranularAuthority = "STORE_" + storeId + "_" + module.getCode().toUpperCase() + "_" + action.toUpperCase();
                            boolean hasGranularPermission = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(requiredGranularAuthority));
                            
                            if (!hasGranularPermission) {
                                throw new AccessDeniedException(messageSource.getMessage("error.employee.cannot.grant.higher.permission", new Object[]{requiredGranularAuthority}, locale));
                            }
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public ResponseEntity<CreateEmployeeResponse> createEmployee(CreateEmployeeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");

        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isOwner) {
            UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());
            EmployeeEntity authEmployee = employeeRepository.findById(authenticatedEmployeeId).orElseThrow();
            validateStoreAndPermissionAssignments(auth, authEmployee, request.getStoreAssignments(), "CREATE");
        }

        String baseUsername = generateBaseUserName(request.getName(), request.getLastName());
        String finalUsername = ensureUniqueUserName(baseUsername);
        
        EmployeeEntity employee = new EmployeeEntity();
        employee.setName(request.getName());
        employee.setLastName(request.getLastName());
        employee.setUsername(finalUsername);
        employee.setPassword(passwordEncoder.encode(generateRandomPassword(10))); 

        syncEmployeeAccess(employee, request.getStoreAssignments());
        EmployeeEntity savedEmployee = employeeRepository.save(employee);

        // Obtenemos el tenant de la sesión de quien está creando al empleado
        String currentTenant = com.nss.pibblest.modules.tenant.TenantContext.getCurrentTenant();
        
        String activationToken = jwtService.generateActivationToken(savedEmployee.getId(), currentTenant);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        if (!isOwner) {
            checkTargetStoresPermission(auth, employee, "UPDATE");
            
            UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());
            EmployeeEntity authEmployee = employeeRepository.findById(authenticatedEmployeeId).orElseThrow();
            validateStoreAndPermissionAssignments(auth, authEmployee, request.getStoreAssignments(), "UPDATE");
        }

        employee.setName(request.getName());
        employee.setLastName(request.getLastName());

        syncEmployeeAccess(employee, request.getStoreAssignments());
        employeeRepository.save(employee);

        // --- SOLUCIÓN AL "STALE TOKEN PROBLEM" ---
        // Borramos la sesión de Redis. El siguiente request del empleado fallará en el 
        // JwtAuthenticationFilter (401 Unauthorized), forzándolo a iniciar sesión de nuevo 
        // y obtener un JWT fresco con su nueva matriz de permisos.
        sessionTrackerService.invalidateUserSession(employee.getId().toString()); 
        // -----------------------------------------

        return ResponseEntity.ok(new UpdateEmployeeResponse(messageSource.getMessage("employee.updated", null, locale)));
    }

    public ResponseEntity<GetAllEmployeesResponse> getAllEmployees(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        Page<EmployeeEntity> entities;
        if (isOwner) {
            entities = employeeRepository.findByDeletedAtIsNull(pageable);
        } else {
            UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());
            EmployeeEntity authEmployee = employeeRepository.findById(authenticatedEmployeeId).orElseThrow();
            
            // Filtra solo las tiendas de las cuales el empleado autenticado tiene permisos de READ en el módulo EMPLOYEES
            Set<Long> authorizedStoreIds = authEmployee.getEmployeeStores().stream()
                    .filter(EmployeeStoreEntity::isActive)
                    .map(es -> es.getStore().getId())
                    .filter(storeId -> {
                        String reqAuth = "STORE_" + storeId + "_MODULE_EMPLOYEES_READ";
                        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(reqAuth));
                    })
                    .collect(Collectors.toSet());

            if (authorizedStoreIds.isEmpty()) {
                entities = Page.empty(pageable);
            } else {
                entities = employeeRepository.findByEmployeeStoresStoreIdInAndDeletedAtIsNull(authorizedStoreIds, pageable);
            }
        }

        Page<EmployeeDto> dtos = entities.map(employeeMapper::toDto);
        return ResponseEntity.ok(new GetAllEmployeesResponse(dtos));
    }

    public ResponseEntity<EmployeeDto> getEmployeeById(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        if (!isOwner) {
            List<EmployeeStoreEntity> targetStores = employee.getEmployeeStores().stream().filter(EmployeeStoreEntity::isActive).toList();
            if (targetStores.isEmpty()) {
                throw new AccessDeniedException("No posees visibilidad sobre empleados sin sucursal.");
            }
            
            // Garantizar que el usuario que consulta tenga permisos READ en al menos una tienda compartida con el objetivo
            boolean hasReadAccess = targetStores.stream().anyMatch(es -> {
                Long storeId = es.getStore().getId();
                String reqAuth = "STORE_" + storeId + "_MODULE_EMPLOYEES_READ";
                return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(reqAuth));
            });

            if (!hasReadAccess) {
                throw new AccessDeniedException("No posees visibilidad de auditoría sobre el empleado especificado.");
            }
        }

        return ResponseEntity.ok(employeeMapper.toDto(employee));
    }

    @Transactional
    public ResponseEntity<Void> deleteEmployee(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        if (!isOwner) {
            checkTargetStoresPermission(auth, employee, "DELETE");
        }

        employee.setDeletedAt(ZonedDateTime.now());
        employee.getEmployeeStores().forEach(es -> es.setActive(false));
        employeeRepository.save(employee);
        
        // --- EXPULSIÓN INMEDIATA ---
        sessionTrackerService.invalidateUserSession(employee.getId().toString());
        // ---------------------------
        
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<ActivateEmployeeResponse> activateEmployeeAccount(String token, String newPassword) {
        try {
            // 1. Extraemos los identificadores esenciales del token
            UUID employeeId = jwtService.extractEmployeeIdFromActivationToken(token);
            String tokenTenant = jwtService.extractTenantFromActivationToken(token);

            if (tokenTenant == null) {
                throw new IllegalArgumentException("El token está corrupto: no contiene el contexto de la organización.");
            }

            // 2. Seteamos el enrutador dinámico en el ThreadLocal antes de inicializar la conexión
            com.nss.pibblest.modules.tenant.TenantContext.setCurrentTenant(tokenTenant);

            // 3. Abrimos el EntityManager aislado (Bypass de OSIV)
            jakarta.persistence.EntityManager isolatedEm = entityManagerFactory.createEntityManager();
            
            try {
                isolatedEm.getTransaction().begin();

                EmployeeEntity employee = isolatedEm.find(EmployeeEntity.class, employeeId);
                if (employee == null) {
                    throw new EntityNotFoundException("Empleado no encontrado");
                }

                employee.setPassword(passwordEncoder.encode(newPassword));
                isolatedEm.merge(employee);

                isolatedEm.getTransaction().commit();
            } catch (Exception e) {
                if (isolatedEm.getTransaction().isActive()) {
                    isolatedEm.getTransaction().rollback();
                }
                throw e;
            } finally {
                isolatedEm.close(); // Liberación de la conexión de vuelta al pool
            }
            
            // 4. Resolvemos el mensaje de éxito desde las propiedades de traducción
            String successMessage = messageSource.getMessage("employee.activated", null, LocaleContextHolder.getLocale());
            
            return ResponseEntity.ok(new ActivateEmployeeResponse(successMessage));

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new IllegalArgumentException("El enlace de activación ha expirado. Solicita uno nuevo.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("El enlace de activación es inválido o ha expirado.");
        } finally {
            // 5. Limpieza absoluta del contexto de hilos
            com.nss.pibblest.modules.tenant.TenantContext.clear();
        }
    }

    @Transactional
    public ResponseEntity<ResendActivationResponse> resendActivationToken(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        // 1. Buscamos al empleado
        EmployeeEntity employee = employeeRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        // 2. Validamos que el usuario actual tenga permisos para modificar a este empleado
        if (!isOwner) {
            checkTargetStoresPermission(auth, employee, "UPDATE");
        }

        // 3. Generamos el nuevo token de 5 minutos y construimos la URL
        String currentTenant = com.nss.pibblest.modules.tenant.TenantContext.getCurrentTenant();
        String activationToken = jwtService.generateActivationToken(employee.getId(), currentTenant);
        String frontendUrl = "http://localhost:4200/auth/activate-account?token=" + activationToken;

        ResendActivationResponse response = new ResendActivationResponse(
            messageSource.getMessage("employee.token.resent", null, locale),
            frontendUrl
        );

        return ResponseEntity.ok(response);
    }

    private void syncEmployeeAccess(EmployeeEntity employee, List<StoreAssignmentRequest> assignments) {
        if (assignments == null) assignments = new java.util.ArrayList<>();

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

        Set<String> requestedPermKeys = new HashSet<>();
        for (StoreAssignmentRequest sa : assignments) {
            if (sa.permissions() != null) {
                for (ModulePermissionRequest mp : sa.permissions()) {
                    if (mp.actions() != null) {
                        for (String action : mp.actions()) {
                            requestedPermKeys.add(sa.storeId() + "_" + mp.moduleId() + "_" + action.toUpperCase());
                        }
                    }
                }
            }
        }

        if (employee.getGranularPermissions() == null) {
            employee.setGranularPermissions(new java.util.ArrayList<>());
        }

        employee.getGranularPermissions().removeIf(p -> {
            String key = p.getStore().getId() + "_" + p.getModule().getId() + "_" + p.getAction();
            return !requestedPermKeys.contains(key);
        });

        Set<String> existingPermKeys = employee.getGranularPermissions().stream()
                .map(p -> p.getStore().getId() + "_" + p.getModule().getId() + "_" + p.getAction())
                .collect(Collectors.toSet());

        for (StoreAssignmentRequest sa : assignments) {
            StoreEntity storeProxy = storeRepository.getReferenceById(sa.storeId());
            
            if (sa.permissions() != null) {
                for (ModulePermissionRequest mp : sa.permissions()) {
                    ModuleEntity moduleProxy = moduleRepository.getReferenceById(mp.moduleId());
                    
                    if (mp.actions() != null) {
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
