package com.nss.pibblest.modules.owners.api.events;

import java.util.UUID;
import org.springframework.modulith.events.Externalized;

@Externalized("owners-registered-topic")
public record OwnerRegisteredEvent (
   UUID id,
   String company,
   String email,
   String schemaName,
   String encodedPassword
){}
