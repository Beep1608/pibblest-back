package com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;

public class UpdateEmployeeRequest {
    
    @NotBlank(message = "{employee.validation.name.required}")
    private String name;
    
    @NotBlank(message = "{employee.validation.lastname.required}")
    private String lastName;
    
    private Set<Long> storeIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Set<Long> getStoreIds() { return storeIds; }
    public void setStoreIds(Set<Long> storeIds) { this.storeIds = storeIds; }
}
