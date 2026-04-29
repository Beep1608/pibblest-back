package com.nss.pibblest.modules.security.internal.infrastructure.data;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OneTimeTokenOwnerRepository extends JpaRepository<OneTimeTokenOwnerEntity, UUID> {
    
    Optional<OneTimeTokenOwnerEntity> findByTokenValue(String tokenValue);

    Optional<OneTimeTokenOwnerEntity> findByOwnerId(UUID ownerId);

    @Modifying
    @Query("UPDATE OneTimeTokenOwnerEntity t SET t.expired = true WHERE t.ownerId = :ownerId AND t.expired = false and t.used = false")
    void expireAllActiveTokensByOwnerId(@Param("ownerId") UUID ownerId);

    Optional<OneTimeTokenOwnerEntity> findTopByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
