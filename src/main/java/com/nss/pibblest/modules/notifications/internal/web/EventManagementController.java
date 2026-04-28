package com.nss.pibblest.modules.notifications.internal.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.notifications.internal.web.requests.retryIncompleteEventsPublications.RetryIncompleteEventPublicationsRequest;
import com.nss.pibblest.modules.notifications.internal.web.requests.retryIncompleteEventsPublications.RetryIncompleteEventPublicationsResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventManagementController {

    private final IncompleteEventPublications incompleteEventsPublications;
    private final EventPublicationRegistry registry;

    public EventManagementController(IncompleteEventPublications incompleteEventPublications, EventPublicationRegistry registry){
        this.incompleteEventsPublications = incompleteEventPublications;
        this.registry = registry;
    }   
    
    @PostMapping("/retry-incomplete")
    public ResponseEntity<RetryIncompleteEventPublicationsResponse> retryIncomplete(@Valid @RequestBody RetryIncompleteEventPublicationsRequest request){

        UUID id = UUID.fromString( request.getEventId());
        registry.findIncompletePublications().stream()
        .filter(pub -> pub.getIdentifier().equals( id))
        .findFirst()
        .ifPresent(pub ->{
            incompleteEventsPublications.resubmitIncompletePublications(p -> p.getIdentifier().equals(id)); 
        });

       RetryIncompleteEventPublicationsResponse response = new RetryIncompleteEventPublicationsResponse("Se proceso de nuevo el evento con id : "+ request.getEventId());

       return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
