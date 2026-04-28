package com.nss.pibblest.modules.owners.api.events;

import java.util.UUID;

public record  OwnerGenerateVerifyToken(String email,UUID id) {}
