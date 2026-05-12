package com.nss.pibblest.modules.employees.internal.web.requests.assignEmployeeToStore;

public class AssignEmployeeToStoreResponse {
    private String message;

    public AssignEmployeeToStoreResponse(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
