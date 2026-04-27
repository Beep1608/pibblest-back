package com.nss.pibblest.modules.tags.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.tags.internal.core.TagService;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagResponse;

import jakarta.validation.Valid;

//TODO: Revisar le manejo de responses correctamente para este modulo
@RestController
@RequestMapping("/api/tags")
public class TagController {
    
    private final TagService tagService;

    public TagController(TagService tagService){
        this.tagService = tagService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateTagResponse> createTag(@Valid @RequestBody CreateTagRequest request){
        
        return tagService.createTag(request);
    }


    @PostMapping("/assign")
    public ResponseEntity<AssignTagResponse> assignTagToStore(@Valid @RequestBody AssignTagRequest request){

        return tagService.assignTagToStore(request);
    }


}
