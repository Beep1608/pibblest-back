package com.nss.pibblest.modules.employees.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.employees.internal.core.EmployeeService;
import com.nss.pibblest.modules.employees.internal.core.EmployeeStoreService;
import com.nss.pibblest.modules.employees.internal.web.requests.assignEmployeeToStore.AssignEmployeeToStoreRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.assignEmployeeToStore.AssignEmployeeToStoreResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeStoreService employeeStoreService;

    public EmployeeController(EmployeeService employeeService, EmployeeStoreService employeeStoreService){
        this.employeeService = employeeService;
        this.employeeStoreService = employeeStoreService;
    }


    @PostMapping()
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request){
        return employeeService.createEmployee(request);
    }






    @PostMapping("/assign")
        public ResponseEntity<AssignEmployeeToStoreResponse> assignEmployeeToStore(
           @Valid @RequestBody AssignEmployeeToStoreRequest request) {
            return this.employeeStoreService.assignEmployeeToStore(request);
    }
}
