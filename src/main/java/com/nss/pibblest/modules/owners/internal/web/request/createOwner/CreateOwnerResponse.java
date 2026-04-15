package com.nss.pibblest.modules.owners.internal.web.request.createOwner;

import java.util.UUID;

public record CreateOwnerResponse (
    UUID id,
    String message
){}
