package com.nss.pibblest.modules.tags.internal.web.requests.products.getAllProductTagsList;

import java.util.List;
import com.nss.pibblest.modules.tags.api.dto.TagForProductDto;

public class GetAllProductTagsListResponse {

    private List<TagForProductDto> tags;

    public GetAllProductTagsListResponse() {
    }

    public GetAllProductTagsListResponse(List<TagForProductDto> tags) {
        this.tags = tags;
    }

    public List<TagForProductDto> getTags() {
        return tags;
    }

    public void setTags(List<TagForProductDto> tags) {
        this.tags = tags;
    }
}
