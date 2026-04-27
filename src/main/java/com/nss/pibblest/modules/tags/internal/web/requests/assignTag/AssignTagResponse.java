package com.nss.pibblest.modules.tags.internal.web.requests.assignTag;

public class AssignTagResponse {

    private String message;

    public AssignTagResponse(){

    }

    public AssignTagResponse(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
