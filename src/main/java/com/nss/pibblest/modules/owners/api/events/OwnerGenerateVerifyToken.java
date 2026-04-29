package com.nss.pibblest.modules.owners.api.events;

import java.util.UUID;

import org.springframework.modulith.events.Externalized;

@Externalized("owners-resend-verification-token")
public record  OwnerGenerateVerifyToken(String email,UUID id) {}
