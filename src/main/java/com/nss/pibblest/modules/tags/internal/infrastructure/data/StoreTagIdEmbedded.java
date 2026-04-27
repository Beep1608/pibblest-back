package com.nss.pibblest.modules.tags.internal.infrastructure.data;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class StoreTagIdEmbedded implements Serializable {
    @Column(name = "store_id")
    private Long storeId;
    @Column(name = "tag_id")
    private Long tagId;

    public StoreTagIdEmbedded() {
    }

    public StoreTagIdEmbedded(Long storeId, Long tagId) {
        this.storeId = storeId;
        this.tagId = tagId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreTagIdEmbedded that = (StoreTagIdEmbedded) o;
        return Objects.equals(storeId, that.storeId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, tagId);
    }

}
