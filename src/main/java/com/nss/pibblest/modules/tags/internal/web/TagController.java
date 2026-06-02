package com.nss.pibblest.modules.tags.internal.web;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.tags.internal.core.TagService;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.getAllTags.GetAllTagsResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct.AssignTagToProductRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct.AssignTagToProductResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct.CreateTagForProductRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct.CreateTagForProductResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.getAllTagsList.GetAllStoreTagsListResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.getAllProductTagsList.GetAllProductTagsListResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.getAllTagsForProducts.GetAllTagsForProductsResponse;

import jakarta.validation.Valid;

//TODO: Revisar le manejo de responses correctamente para este modulo
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateTagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {

        return tagService.createTag(request);
    }

    @PostMapping("/assign")
    public ResponseEntity<AssignTagResponse> assignTagToStore(@Valid @RequestBody AssignTagRequest request) {

        return tagService.assignTagToStore(request);
    }

    @GetMapping("/all")
    public ResponseEntity<GetAllTagsResponse> getAllTags(Pageable pageable) {

        return tagService.getAllTags(pageable);
    }

    @PostMapping("/assign-to-product")
    public ResponseEntity<AssignTagToProductResponse> assignTagToProduct(
            @Valid @RequestBody AssignTagToProductRequest request) {
        return tagService.assignTagToProduct(request);
    }

    @PostMapping("/create-to-products")
    public ResponseEntity<CreateTagForProductResponse> createTagToProduct(@Valid @RequestBody CreateTagForProductRequest request){
        return tagService.createTagToProduct(request);
    }

    @GetMapping("/get-all-products")
    public ResponseEntity<GetAllTagsForProductsResponse> getAllTagsForProducts(String keyword, Pageable pageable){
        return  tagService.getAllTagsForProducts(keyword, pageable);
    } 

    @GetMapping("/products/list")
    public ResponseEntity<GetAllProductTagsListResponse> getAllProductTagsList() {
        return tagService.getAllProductTagsList();
    }

    @GetMapping("/stores/list")
    public ResponseEntity<GetAllStoreTagsListResponse> getAllStoreTagsList() {
        return tagService.getAllStoreTagsList();
    }

}
