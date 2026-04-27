package com.nss.pibblest.modules.tags.internal.core;



import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.stores.internal.core.exceptions.StoreNotFound;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.core.exceptions.TagsNotFound;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagRepository;
import com.nss.pibblest.modules.tags.internal.mappers.TagMapper;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.getAllTags.GetAllTagsResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class TagService {
    
    private final TagRepository tagRepository;
    private final StoreRepository storeRepository;
    private final MessageSource messageSource;
    private final StoreTagRepository storeTagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, StoreRepository storeRepository, MessageSource messageSource, StoreTagRepository storeTagRepository, TagMapper tagMapper){
        this.tagRepository = tagRepository;
        this.storeRepository = storeRepository;
        this.messageSource = messageSource;
        this.storeTagRepository = storeTagRepository;
        this.tagMapper = tagMapper;
    }

    public ResponseEntity<CreateTagResponse> createTag(@Valid @RequestBody CreateTagRequest request){

        TagEntity tagEntityRequest = new TagEntity(request.getName());

        TagEntity newTagEntity =  tagRepository.save(tagEntityRequest);

       CreateTagResponse response = new CreateTagResponse(newTagEntity);

       return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Transactional
    public ResponseEntity<AssignTagResponse> assignTagToStore(@Valid @RequestBody AssignTagRequest request){

        validateTagsExist(request.getTagsId());

        
        StoreEntity storeEntity = storeRepository.findById(request.getStoreId()).orElseThrow(() -> new StoreNotFound(
            messageSource.getMessage("error.store.not.found", new Object[]{ request.getStoreId()}, LocaleContextHolder.getLocale())
        ));

       List<StoreTagEntity> newAssignments = request.getTagsId().stream().map(tagId -> {
            TagEntity tagProxy = tagRepository.getReferenceById(tagId);

            return new StoreTagEntity(storeEntity, tagProxy);
        })
        .collect(Collectors.toList());

        storeTagRepository.saveAll(newAssignments);

        
        AssignTagResponse response = new AssignTagResponse( messageSource.getMessage("tags.assigned.correctly", null, LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        
    }

    public ResponseEntity<GetAllTagsResponse> getAllTags(Pageable pageable){
        Page<TagEntity> tagsPage = tagRepository.findAll(pageable);

        Page<TagDto> dtoPage = tagsPage.map(tagMapper::toDto);
        GetAllTagsResponse response = new GetAllTagsResponse( dtoPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private boolean  validateTagsExist(Set<Long> requestedTagIds) {

        List<TagEntity> existingTags = tagRepository.findAllById(requestedTagIds);


        if (existingTags.size() != requestedTagIds.size()) {

            Set<Long> foundIds = existingTags.stream()
                    .map(TagEntity::getId)
                    .collect(Collectors.toSet());


            Set<Long> missingIds = new HashSet<>(requestedTagIds);
            missingIds.removeAll(foundIds);

            String errorMessage = messageSource.getMessage("error.tags.not.found", new Object[]{missingIds.toString()} ,LocaleContextHolder.getLocale());

            throw new TagsNotFound(errorMessage);
        }
        return true;
    }

}
