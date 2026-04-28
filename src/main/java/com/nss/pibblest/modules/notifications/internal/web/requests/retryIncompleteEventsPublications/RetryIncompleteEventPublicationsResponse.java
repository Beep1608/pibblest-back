package com.nss.pibblest.modules.notifications.internal.web.requests.retryIncompleteEventsPublications;

public class RetryIncompleteEventPublicationsResponse {

    private String message;

    public RetryIncompleteEventPublicationsResponse(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
