package com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee;

public class ActivateEmployeeResponse {
    private String message;

    public ActivateEmployeeResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
