package com.nss.pibblest.modules.tags.internal.web.requests.createGroup;

import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;

public class CreateTagResponse {
    TagEntity tag;

    public CreateTagResponse( TagEntity tag) {
        this.tag = tag;
    }

    public TagEntity getTag() {
        return tag;
    }

    public void setTag(TagEntity tag) {
        this.tag = tag;
    }

}
