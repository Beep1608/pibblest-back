package com.nss.pibblest.modules.employees.internal.core;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeStoreEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeStoreRepository;
import com.nss.pibblest.modules.employees.internal.web.requests.assignEmployeeToStore.AssignEmployeeToStoreRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.assignEmployeeToStore.AssignEmployeeToStoreResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class EmployeeStoreService {

    private final EmployeeStoreRepository employeeStoreRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final MessageSource messageSource;

    public EmployeeStoreService(EmployeeStoreRepository employeeStoreRepository,
            StoreRepository storeRepository,
            EmployeeRepository employeeRepository,
            MessageSource messageSource) {
        this.employeeStoreRepository = employeeStoreRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
        this.messageSource = messageSource;
    }

    @Transactional
    public ResponseEntity<AssignEmployeeToStoreResponse> assignEmployeeToStore(
            AssignEmployeeToStoreRequest request) {
        Locale locale = LocaleContextHolder.getLocale();

        StoreEntity storeEntity = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("error.store.not.found",
                        new Object[] { request.getStoreId() }, locale)));

        if (!storeEntity.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new IllegalStateException(messageSource.getMessage("store.inactive", null, locale));
        }

        Set<UUID> requestEmployeeIds = request.getEmployees()
                .stream()
                .collect(Collectors.toSet());

        List<EmployeeEntity> employees = employeeRepository.findAllById(requestEmployeeIds);

        if (employees.size() != requestEmployeeIds.size()) {
            Set<UUID> foundIds = employees.stream()
                    .map(EmployeeEntity::getId)
                    .collect(Collectors.toSet());

            requestEmployeeIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .ifPresent(missingId -> {
                        throw new EntityNotFoundException(
                                messageSource
                                        .getMessage("employee.not.found", new Object[] { missingId }, locale));
                    });
        }

        List<EmployeeStoreEntity> newAssigments = employees.stream()
                .map(employee -> new EmployeeStoreEntity(storeEntity, employee, true))
                .toList();

        List<EmployeeStoreEntity> alreadyRegistered = employeeStoreRepository
                .findByEmployee_IdIn(employees.stream().map(e -> e.getId()).toList());

        if (!alreadyRegistered.isEmpty()) {

            String assignedUsernames = alreadyRegistered.stream()
                    .map(m -> m.getEmployee().getUsername())
                    .collect(Collectors.joining(", "));

            throw new IllegalStateException(
                    messageSource.getMessage("employees.already.assigned", new Object[] { assignedUsernames }, locale));
        }
        System.out.println("No se encontró alguno");
        employeeStoreRepository.saveAll(newAssigments);

        AssignEmployeeToStoreResponse response = new AssignEmployeeToStoreResponse(
                messageSource.getMessage("employees.assigment.done", null, locale));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
