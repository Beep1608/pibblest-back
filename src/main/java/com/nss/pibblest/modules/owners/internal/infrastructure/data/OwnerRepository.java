package com.nss.pibblest.modules.owners.internal.infrastructure.data;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<OwnerEntity, UUID> {
    
    boolean existsByEmail(String email);
    boolean existsByOrganizationCode(String organizationCode);

    boolean existsByCompany(String company);
}
