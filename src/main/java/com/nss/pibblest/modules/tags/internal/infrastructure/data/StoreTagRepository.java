package com.nss.pibblest.modules.tags.internal.infrastructure.data;

import java.util.List;

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

    @Query("SELECT st FROM StoreTagEntity st JOIN FETCH st.tagEntity WHERE st.storeEntity.id IN :storeIds")
    List<StoreTagEntity> findTagsByStoreIds(@Param("storeIds") List<Long> storeIds);
}
