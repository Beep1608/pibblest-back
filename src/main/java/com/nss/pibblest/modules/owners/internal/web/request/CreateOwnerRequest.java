package com.nss.pibblest.modules.owners.internal.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOwnerRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8) String password,
    @Size(max = 10) String organizationCode,
    @Size(max = 63) String schemaName
) {}