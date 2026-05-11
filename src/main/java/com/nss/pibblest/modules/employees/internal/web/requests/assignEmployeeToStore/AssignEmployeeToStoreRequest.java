package com.nss.pibblest.modules.employees.internal.web.requests.assignEmployeeToStore;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class AssignEmployeeToStoreRequest {

    private Long storeId;
    private List<EmployeeItemRequest> employees;

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public List<EmployeeItemRequest> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeItemRequest> employees) {
        this.employees = employees;
    }

    public static class EmployeeItemRequest {
        @NotNull(message = "El ID del producto es obligatorio")
        private UUID employeeId;

        public UUID getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(UUID employeeId) {
            this.employeeId = employeeId;
        }

    }
}
