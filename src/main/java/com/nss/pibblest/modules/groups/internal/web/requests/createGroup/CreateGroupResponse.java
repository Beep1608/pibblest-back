package com.nss.pibblest.modules.groups.internal.web.requests.createGroup;

import java.util.List;

import com.nss.pibblest.modules.groups.internal.infrastructure.data.GroupEntity;

public class CreateGroupResponse {
    List<GroupEntity> entitites ;

    public CreateGroupResponse( List<GroupEntity> entitites) {
        this.entitites = entitites;
    }

    public List<GroupEntity> getEntitites() {
        return entitites;
    }

    public void setEntitites(List<GroupEntity> entitites) {
        this.entitites = entitites;
    }

}
