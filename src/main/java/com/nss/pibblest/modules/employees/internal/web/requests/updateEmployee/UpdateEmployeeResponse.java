package com.nss.pibblest.modules.employees.internal.web.requests.updateEmployee;

public class UpdateEmployeeResponse {
    private String message;

    public UpdateEmployeeResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
