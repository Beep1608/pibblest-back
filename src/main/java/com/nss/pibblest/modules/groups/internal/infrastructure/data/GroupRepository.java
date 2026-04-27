package com.nss.pibblest.modules.groups.internal.infrastructure.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    
}
