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
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeStoreEntity;
import com.nss.pibblest.modules.employees.internal.mappers.EmployeeMapper;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.getAllEmployees.GetAllEmployeesResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final EmployeeMapper employeeMapper;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%-";

    public EmployeeService(EmployeeRepository employeeRepository, StoreRepository storeRepository, 
                           PasswordEncoder passwordEncoder, MessageSource messageSource, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public ResponseEntity<CreateEmployeeResponse> createEmployee(CreateEmployeeRequest request) {
        String baseUsername = generateBaseUserName(request.getName(), request.getLastName());
        String finalUsername = ensureUniqueUserName(baseUsername);
        String plainPassword = generateRandomPassword(10);
        
        EmployeeEntity employee = new EmployeeEntity();
        employee.setName(request.getName());
        employee.setLastName(request.getLastName());
        employee.setUsername(finalUsername);
        employee.setPassword(passwordEncoder.encode(plainPassword));

        syncEmployeeStores(employee, request.getStoreIds());

        EmployeeEntity savedEmployee = employeeRepository.save(employee);

        CreateEmployeeResponse response = new CreateEmployeeResponse(
            messageSource.getMessage("employee.created", new Object[]{savedEmployee.getUsername()}, LocaleContextHolder.getLocale())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Transactional
    public ResponseEntity<UpdateEmployeeResponse> editEmployee(UUID id, UpdateEmployeeRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        employee.setName(request.getName());
        employee.setLastName(request.getLastName());

        syncEmployeeStores(employee, request.getStoreIds());

        employeeRepository.save(employee);

        return ResponseEntity.ok(new UpdateEmployeeResponse(
            messageSource.getMessage("employee.updated", null, locale)
        ));
    }

    public ResponseEntity<GetAllEmployeesResponse> getAllEmployees(Pageable pageable) {
        Page<EmployeeEntity> entities = employeeRepository.findAll(pageable);
        Page<EmployeeDto> dtos = entities.map(employeeMapper::toDto);
        return ResponseEntity.ok(new GetAllEmployeesResponse(dtos));
    }

    public ResponseEntity<EmployeeDto> getEmployeeById(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));
        return ResponseEntity.ok(employeeMapper.toDto(employee));
    }

    @Transactional
    public ResponseEntity<Void> deleteEmployee(UUID id) {
        Locale locale = LocaleContextHolder.getLocale();
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFound("employee.not.found", new Object[]{id}, locale));

        employee.setDeletedAt(ZonedDateTime.now());
        // Soft delete para las asociaciones de tienda también
        employee.getEmployeeStores().forEach(es -> es.setActive(false));
        
        employeeRepository.save(employee);
        
        return ResponseEntity.noContent().build();
    }

    private void syncEmployeeStores(EmployeeEntity employee, Set<Long> requestedStoreIds) {
        Set<Long> storeIds = requestedStoreIds == null ? new HashSet<>() : requestedStoreIds;

        // 1. Eliminar asociaciones que ya no están solicitadas
        employee.getEmployeeStores().removeIf(es -> !storeIds.contains(es.getStore().getId()));

        // 2. Identificar tiendas existentes
        Set<Long> existingStoreIds = employee.getEmployeeStores().stream()
                .map(es -> es.getStore().getId())
                .collect(Collectors.toSet());

        // 3. Agregar nuevas tiendas validando existencia
        for (Long storeId : storeIds) {
            if (!existingStoreIds.contains(storeId)) {
                StoreEntity store = storeRepository.findById(storeId)
                        .orElseThrow(() -> new EntityNotFoundException(
                            messageSource.getMessage("error.store.not.found", new Object[]{storeId}, LocaleContextHolder.getLocale())
                        ));
                employee.getEmployeeStores().add(new EmployeeStoreEntity(store, employee, true));
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
        while(employeeRepository.existsByUsername(username)){
            username = baseUsername+counter;
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
