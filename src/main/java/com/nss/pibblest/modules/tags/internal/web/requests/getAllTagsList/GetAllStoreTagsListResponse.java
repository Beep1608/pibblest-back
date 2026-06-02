package com.nss.pibblest.modules.tags.internal.web.requests.getAllTagsList;

import java.util.List;
import com.nss.pibblest.modules.tags.api.dto.TagDto;

public class GetAllStoreTagsListResponse {

    private List<TagDto> tags;

    public GetAllStoreTagsListResponse() {
    }

    public GetAllStoreTagsListResponse(List<TagDto> tags) {
        this.tags = tags;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }
}
