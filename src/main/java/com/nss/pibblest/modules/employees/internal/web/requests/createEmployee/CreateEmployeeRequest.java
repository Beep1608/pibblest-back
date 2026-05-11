package com.nss.pibblest.modules.employees.internal.web.requests.createEmployee;

public class CreateEmployeeRequest {
    private String name;
    private String lastName;
    public CreateEmployeeRequest(String name, String lastName) {
        this.name = name;
        this.lastName = lastName;
    }
    public CreateEmployeeRequest() {
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
