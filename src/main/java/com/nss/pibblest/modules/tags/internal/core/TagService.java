package com.nss.pibblest.modules.tags.internal.core;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;
import com.nss.pibblest.modules.stores.internal.core.exceptions.StoreNotFound;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.core.exceptions.TagsNotFound;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagProductEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagProductRepository;
import com.nss.pibblest.modules.tags.internal.mappers.TagMapper;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.getAllTags.GetAllTagsResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct.AssignTagToProductRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct.AssignTagToProductResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct.CreateTagForProductRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct.CreateTagForProductResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.getAllTagsForProducts.GetAllTagsForProductsResponse;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final StoreRepository storeRepository;
    private final MessageSource messageSource;
    private final StoreTagRepository storeTagRepository;
    private final TagForProductsRepository tagForProductsRepository;
    private final ProductRespository productRespository;
    private final TagProductRepository tagProductRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, StoreRepository storeRepository, MessageSource messageSource,
            StoreTagRepository storeTagRepository,
            TagForProductsRepository tagForProductsRepository,
            ProductRespository productRespository,
            TagProductRepository tagProductRepository,
            TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.storeRepository = storeRepository;
        this.messageSource = messageSource;
        this.storeTagRepository = storeTagRepository;
        this.tagForProductsRepository = tagForProductsRepository;
        this.productRespository = productRespository;
        this.tagProductRepository = tagProductRepository;
        this.tagMapper = tagMapper;
    }

    public ResponseEntity<CreateTagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {

        TagEntity tagEntityRequest = new TagEntity(request.getName());

        TagEntity newTagEntity = tagRepository.save(tagEntityRequest);

        CreateTagResponse response = new CreateTagResponse(newTagEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Transactional
    public ResponseEntity<AssignTagResponse> assignTagToStore(@Valid @RequestBody AssignTagRequest request) {

        validateTagsExistForStore(request.getTagsId());

        StoreEntity storeEntity = storeRepository.findById(request.getStoreId()).orElseThrow(() -> new StoreNotFound(
                messageSource.getMessage("store.not.found", new Object[] { request.getStoreId() },
                        LocaleContextHolder.getLocale())));

        List<StoreTagEntity> newAssignments = request.getTagsId().stream().map(tagId -> {
            TagEntity tagProxy = tagRepository.getReferenceById(tagId);

            return new StoreTagEntity(storeEntity, tagProxy);
        })
                .collect(Collectors.toList());

        storeTagRepository.saveAll(newAssignments);

        AssignTagResponse response = new AssignTagResponse(
                messageSource.getMessage("tags.assigned.correctly", null, LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    public ResponseEntity<GetAllTagsResponse> getAllTags(Pageable pageable) {
        Page<TagEntity> tagsPage = tagRepository.findAll(pageable);

        Page<TagDto> dtoPage = tagsPage.map(tagMapper::toDto);
        GetAllTagsResponse response = new GetAllTagsResponse(dtoPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private boolean validateTagsExistForStore(Set<Long> requestedTagIds) {

        List<TagEntity> existingTags = tagRepository.findAllById(requestedTagIds);

        if (existingTags.size() != requestedTagIds.size()) {

            Set<Long> foundIds = existingTags.stream()
                    .map(TagEntity::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = new HashSet<>(requestedTagIds);
            missingIds.removeAll(foundIds);

            String errorMessage = messageSource.getMessage("error.tags.not.found",
                    new Object[] { missingIds.toString() }, LocaleContextHolder.getLocale());

            throw new TagsNotFound(errorMessage);
        }
        return true;
    }

    @Transactional
    public ResponseEntity<CreateTagForProductResponse> createTagToProduct(CreateTagForProductRequest request) {
        Locale locale = LocaleContextHolder.getLocale();

        TagForProductsEntity tagForProductsEntity = new TagForProductsEntity(request.getName());

        TagForProductsEntity newTagForProductsEntity = tagForProductsRepository.save(tagForProductsEntity);

        CreateTagForProductResponse response = new CreateTagForProductResponse(
                messageSource.getMessage("tag.for.products.created",
                        new Object[] { newTagForProductsEntity.getName() }, locale));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Transactional
    public ResponseEntity<AssignTagToProductResponse> assignTagToProduct(AssignTagToProductRequest request) {
        validateTagsExistForProducts(request.getTagsId());

        ProductEntity productEntity = productRespository.findById(request.getProductId()).orElseThrow(
                () -> new EntityNotFoundException(messageSource.getMessage(
                        "products.not.found",
                        new Object[] { request.getProductId() },
                        LocaleContextHolder.getLocale())));

        List<TagProductEntity> newAssigments = request.getTagsId().stream().map(tagId -> {
            TagForProductsEntity tagProxy = tagForProductsRepository.getReferenceById(tagId);

            return new TagProductEntity(productEntity, tagProxy);
        })
                .collect(Collectors.toList());

        tagProductRepository.saveAll(newAssigments);

        AssignTagToProductResponse response = new AssignTagToProductResponse(
                messageSource.getMessage("tag.for.products.assigned", null, LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    private boolean validateTagsExistForProducts(Set<Long> requestedTagIds) {
        List<TagForProductsEntity> existingTags = tagForProductsRepository.findAllById(requestedTagIds);

        if (existingTags.size() != requestedTagIds.size()) {
            Set<Long> foundIds = existingTags.stream()
                    .map(TagForProductsEntity::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = new HashSet<>(requestedTagIds);
            missingIds.removeAll(foundIds);

            String errorMessage = messageSource.getMessage("error.tags.not.found",
                    new Object[] { missingIds.toString() }, LocaleContextHolder.getLocale());

            throw new TagsNotFound(errorMessage);
        }
        return true;
    }

    public ResponseEntity<GetAllTagsForProductsResponse> getAllTagsForProducts(
            String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            Page<TagForProductsEntity> tagForProductsPage = tagForProductsRepository.findAll(pageable);

            Page<TagDto> dtoPage = tagForProductsPage.map(tagMapper::fromTagForProductToDto);
            GetAllTagsForProductsResponse response = new GetAllTagsForProductsResponse(dtoPage);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        Page<TagForProductsEntity> tagForProductsPage = tagForProductsRepository.findByNameContainingIgnoreCase(keyword,
                pageable);

        Page<TagDto> dtoPage = tagForProductsPage.map(tagMapper::fromTagForProductToDto);
        GetAllTagsForProductsResponse response = new GetAllTagsForProductsResponse(dtoPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
