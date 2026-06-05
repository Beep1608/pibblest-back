package com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import com.nss.pibblest.modules.employees.internal.web.requests.StoreAssignmentRequest;

public class UpdateEmployeeRequest {
    
    @NotBlank(message = "{employee.validation.name.required}")
    private String name;
    
    @NotBlank(message = "{employee.validation.lastname.required}")
    private String lastName;
    
    private List<StoreAssignmentRequest> storeAssignments;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public List<StoreAssignmentRequest> getStoreAssignments() { return storeAssignments; }
    public void setStoreAssignments(List<StoreAssignmentRequest> storeAssignments) { this.storeAssignments = storeAssignments; }
}
