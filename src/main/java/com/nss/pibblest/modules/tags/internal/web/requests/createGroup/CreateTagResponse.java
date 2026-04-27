package com.nss.pibblest.modules.tags.internal.web.requests.createGroup;

import java.util.List;

import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;

public class CreateTagResponse {
    List<TagEntity> entitites ;

    public CreateTagResponse( List<TagEntity> entitites) {
        this.entitites = entitites;
    }

    public List<TagEntity> getEntitites() {
        return entitites;
    }

    public void setEntitites(List<TagEntity> entitites) {
        this.entitites = entitites;
    }

}
