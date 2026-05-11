package com.nss.pibblest.modules.employees.internal.web.requests.createEmployee;

public class CreateEmployeeResponse {
    private String message;

    public CreateEmployeeResponse(String message){
        this.message = message; 
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
