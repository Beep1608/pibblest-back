package com.nss.pibblest.modules.owners.internal.infrastructure.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OwnerRepository extends JpaRepository<OwnerEntity, UUID> {
    
    boolean existsByEmail(String email);
    boolean existsByOrganizationCode(String organizationCode);

    boolean existsByCompany(String company);

    Optional<OwnerEntity> findEntityByOrganizationCode(String organizationCode);
    Optional<OwnerEntity> findByEmail(String email);

    Optional<OwnerEntity>  findByCompany(String company);
}
