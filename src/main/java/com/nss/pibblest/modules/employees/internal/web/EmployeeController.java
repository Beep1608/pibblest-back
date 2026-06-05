package com.nss.pibblest.modules.employees.internal.web;

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
import com.nss.pibblest.modules.employees.internal.core.EmployeeService;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.getAllEmployees.GetAllEmployeesResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.UpdateEmployeeResponse;
import com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee.ActivateEmployeeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Empleados", description = "Endpoints para la gestión del personal, asignación de tiendas y control de permisos granulares por módulo.")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService){
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasAuthority('CAN_SEE')")
    @Operation(
        summary = "Listar empleados (Paginado)", 
        description = "Obtiene una lista paginada de todos los empleados activos en la organización (excluye registros con borrado lógico)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado de empleados devuelto exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol OWNER o autoridad CAN_SEE)")
    })
    public ResponseEntity<GetAllEmployeesResponse> getAllEmployees(
            @Parameter(description = "Parámetros de paginación (page, size, sort)") Pageable pageable) {
        return employeeService.getAllEmployees(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasAuthority('CAN_READ')")
    @Operation(
        summary = "Obtener detalle de empleado", 
        description = "Devuelve los detalles de un empleado en específico, incluyendo la lista de tiendas asignadas y sus permisos granulares detallados."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalles del empleado obtenidos correctamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol OWNER o autoridad CAN_READ)"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public ResponseEntity<EmployeeDto> getEmployeeById(
            @Parameter(description = "UUID único del empleado a consultar") @PathVariable("id") UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')") // Permite el ingreso para la evaluación dinámica en el servicio
    @Operation(
        summary = "Crear nuevo empleado", 
        description = "Registra un nuevo empleado en el sistema. Si la operación es realizada por un empleado administrativo, se validará que tenga permisos de creación en cada tienda asignada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación o tienda no encontrada"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (No tienes permisos en alguna de las tiendas)")
    })
    public ResponseEntity<CreateEmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request){
        return employeeService.createEmployee(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasAuthority('CAN_EDIT')")
    @Operation(
        summary = "Editar empleado", 
        description = "Actualiza la información personal de un empleado y sincroniza (crea, actualiza o elimina) su asignación a tiendas y permisos de módulos en base a la matriz proporcionada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Empleado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en los datos enviados"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol OWNER o autoridad CAN_EDIT)"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public ResponseEntity<UpdateEmployeeResponse> editEmployee(
           @Parameter(description = "UUID único del empleado a editar") @PathVariable("id") UUID id, 
           @Valid @RequestBody UpdateEmployeeRequest request) {
            return employeeService.editEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasAuthority('CAN_DELETE')")
    @Operation(
        summary = "Eliminar empleado (Soft Delete)", 
        description = "Realiza un borrado lógico del empleado y desactiva todas sus relaciones con las tiendas, revocando así su acceso al sistema sin perder historial."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Empleado eliminado correctamente (Sin contenido)"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Requiere rol OWNER o autoridad CAN_DELETE)"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "UUID único del empleado a eliminar") @PathVariable("id") UUID id) {
        return employeeService.deleteEmployee(id);
    }

    @PostMapping("/activate")
    // OJO: No lleva @PreAuthorize porque es un endpoint público
    @Operation(summary = "Activar cuenta de empleado", description = "Recibe el token de activación y establece la contraseña definitiva del empleado.")
    public ResponseEntity<Void> activateAccount(@Valid @RequestBody ActivateEmployeeRequest request) {
        return employeeService.activateEmployeeAccount(request.getToken(), request.getNewPassword());
    }
}
