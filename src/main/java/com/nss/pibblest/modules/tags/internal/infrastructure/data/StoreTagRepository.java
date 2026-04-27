package com.nss.pibblest.modules.tags.internal.infrastructure.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  StoreTagRepository extends JpaRepository<StoreTagEntity, StoreTagIdEmbedded> {
    
    @Modifying
    @Query("DELETE FROM StoreTagEntity st WHERE st.id.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
