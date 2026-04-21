package com.nss.pibblest.modules.security.internal.infrastructure.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OneTimeTokenOwnerRepository extends JpaRepository<OneTimeTokenOwnerEntity, UUID> {
    
    Optional<OneTimeTokenOwnerEntity> findByTokenValue(String tokenValue);
}
