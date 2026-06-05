package com.nss.pibblest.modules.employees.internal.web.requests.createEmployee;

public class CreateEmployeeResponse {
    private String message;
    private String activationLink; // NUEVO CAMPO

    public CreateEmployeeResponse(String message, String activationLink){
        this.message = message; 
        this.activationLink = activationLink;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getActivationLink() { return activationLink; }
    public void setActivationLink(String activationLink) { this.activationLink = activationLink; }
}
