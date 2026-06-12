package com.nss.pibblest.modules.employees.internal.web;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.employees.api.dto.EmployeeDto;
import com.nss.pibblest.modules.employees.api.dto.EmployeeSimpleDto;
import com.nss.pibblest.modules.employees.internal.core.EmployeeService;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.getAllEmployees.GetAllEmployeesResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.ActivateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.ActivateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.ResendActivationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Empleados", description = "Endpoints para la gestión del personal y permisos granulares.")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Listar empleados (Paginado)")
    public ResponseEntity<GetAllEmployeesResponse> getAllEmployees(Pageable pageable) {
        return employeeService.getAllEmployees(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Obtener detalle de empleado")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable("id") UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Crear nuevo empleado")
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request){
        return employeeService.createEmployee(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Editar empleado")
    public ResponseEntity<UpdateEmployeeResponse> editEmployee(
           @PathVariable("id") UUID id, 
           @Valid @RequestBody UpdateEmployeeRequest request) {
            return employeeService.editEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Eliminar empleado (Soft Delete)")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") UUID id) {
        return employeeService.deleteEmployee(id);
    }

    @PostMapping("/activate")
    @Operation(summary = "Activar cuenta de empleado", description = "Recibe el token de activación, establece la contraseña definitiva del empleado y confirma el éxito de la operación.")
    public ResponseEntity<ActivateEmployeeResponse> activateAccount(@Valid @RequestBody ActivateEmployeeRequest request) {
        return employeeService.activateEmployeeAccount(request.getToken(), request.getNewPassword());
    }

    @PostMapping("/{id}/resend-activation")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(
        summary = "Reenviar enlace de activación", 
        description = "Genera un nuevo token de 5 minutos y devuelve un nuevo enlace de activación para un empleado existente."
    )
    public ResponseEntity<ResendActivationResponse> resendActivation(@PathVariable("id") UUID id) {
        return employeeService.resendActivationToken(id);
    }

    @GetMapping("/store/{storeId}/simple")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @Operation(
        summary = "Listado simple de empleados por tienda", 
        description = "Devuelve exclusivamente el ID y username de los empleados activos asignados a una sucursal específica. Retorno en formato de lista sin paginación."
    )
    public ResponseEntity<List<EmployeeSimpleDto>> getSimpleEmployeesByStore(@PathVariable("storeId") Long storeId) {
        return employeeService.getSimpleEmployeesByStore(storeId);
    }
}
