package com.nss.pibblest.modules.tags.internal.infrastructure.data;

import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name ="stores_tags")
public class StoreTagEntity {
    
    @EmbeddedId
    private StoreTagIdEmbedded id = new StoreTagIdEmbedded();

    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name="store_id")
    private StoreEntity storeEntity;

    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name="tag_id")
    private TagEntity tagEntity;

    public StoreTagEntity(){}

    public StoreTagEntity(StoreEntity storeEntity, TagEntity tagEntity){
        this.storeEntity = storeEntity;
        this.tagEntity = tagEntity;
    }

    public StoreTagIdEmbedded getId() {
        return id;
    }

    public void setId(StoreTagIdEmbedded id) {
        this.id = id;
    }

    public StoreEntity getStoreEntity() {
        return storeEntity;
    }

    public void setStoreEntity(StoreEntity storeEntity) {
        this.storeEntity = storeEntity;
    }

    public TagEntity getTagEntity() {
        return tagEntity;
    }

    public void setTagEntity(TagEntity tagEntity) {
        this.tagEntity = tagEntity;
    }

    
}
