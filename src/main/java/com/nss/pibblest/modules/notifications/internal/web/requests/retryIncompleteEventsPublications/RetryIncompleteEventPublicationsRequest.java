package com.nss.pibblest.modules.notifications.internal.web.requests.retryIncompleteEventsPublications;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public class RetryIncompleteEventPublicationsRequest {

    @NotNull(message="El eventId no puede ser nulo")
    @JsonProperty("eventId")
    private String eventId;

    public RetryIncompleteEventPublicationsRequest(){}

    public RetryIncompleteEventPublicationsRequest(String eventId){
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


    
    
    
    
}
