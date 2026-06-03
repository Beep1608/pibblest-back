package com.nss.pibblest.modules.stores.internal.core;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.stores.api.dtos.StoreDto;
import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;
import com.nss.pibblest.modules.stores.api.dtos.StoreSimpleDto;
import com.nss.pibblest.modules.stores.internal.core.exceptions.StoreNotFound;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.stores.internal.mappers.StoreMapper;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.deleteStore.DeleteStoreResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.getAllStores.GetAllStoresResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreResponse;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.core.exceptions.TagsNotFound;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.StoreTagRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.TagRepository;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final MessageSource messageSource;
    private final TagRepository tagRepository;
    private final StoreTagRepository storeTagRepository;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, MessageSource messageSource,
                        TagRepository tagRepository, StoreTagRepository storeTagRepository) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.messageSource = messageSource;
        this.tagRepository = tagRepository;
        this.storeTagRepository = storeTagRepository;
    }

    public ResponseEntity<GetAllStoresResponse> getAllStores(Pageable pageable) {
        ZonedDateTime startOfToday = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Page<StorePreviewDto> storesPage = storeRepository.findStorePreviewInfo(pageable, startOfToday);
        Locale locale = LocaleContextHolder.getLocale();

        storesPage.forEach(dto -> {
            String localizedTime = getLocalizedOperatingTime(dto.getCreatedAt(), locale);
            dto.setOperatinTime(localizedTime);
        });
        GetAllStoresResponse response = new GetAllStoresResponse(storesPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Hallazgo #1: Obtener la lista simple de todas las tiendas (Id, Name)
    @Transactional(readOnly = true)
    public ResponseEntity<List<StoreSimpleDto>> getAllStoresSimple() {
        return ResponseEntity.ok(storeRepository.findAllSimpleStores());
    }

    // Hallazgo #2: Búsqueda segura por palabra clave y extracción de Tags
    @Transactional(readOnly = true)
    public ResponseEntity<GetAllStoresResponse> searchStores(String keyword, Pageable pageable) {
        ZonedDateTime startOfToday = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Page<StorePreviewDto> storesPage = storeRepository.findStorePreviewInfoByKeyword(pageable, startOfToday, keyword);
        Locale locale = LocaleContextHolder.getLocale();

        List<Long> storeIds = storesPage.getContent().stream().map(StorePreviewDto::getId).collect(Collectors.toList());

        if (!storeIds.isEmpty()) {
            List<StoreTagEntity> storeTags = storeTagRepository.findTagsByStoreIds(storeIds);
            
            Map<Long, List<TagDto>> tagsByStore = storeTags.stream()
                    .collect(Collectors.groupingBy(
                            st -> st.getStoreEntity().getId(),
                            Collectors.mapping(st -> new TagDto(st.getTagEntity().getName(), st.getTagEntity().getId(),0L), Collectors.toList())
                    ));

            storesPage.forEach(dto -> {
                dto.setTags(tagsByStore.getOrDefault(dto.getId(), new ArrayList<>()));
                String localizedTime = getLocalizedOperatingTime(dto.getCreatedAt(), locale);
                dto.setOperatinTime(localizedTime);
            });
        }

        GetAllStoresResponse response = new GetAllStoresResponse(storesPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<CreateStoreResponse> createStore(CreateStoreRequest request) {

        StoreEntity storeEntity = storeMapper.toEntity(request);
        StoreEntity newStoreEntity = storeRepository.save(storeEntity);

        if (request.getTagsId() != null && !request.getTagsId().isEmpty()) {
            validateTags(request.getTagsId());
            for (Long tagId : request.getTagsId()) {
                TagEntity tag = tagRepository.getReferenceById(tagId);
                newStoreEntity.getStoreTags().add(new StoreTagEntity(newStoreEntity, tag));
            }
            newStoreEntity = storeRepository.save(newStoreEntity);
        }

        // Hallazgo #3: Retorna el objeto DTO completo mapeado
        StoreDto storeDto = storeMapper.toDto(newStoreEntity);
        String message = messageSource.getMessage("store.created.response", new Object[] { newStoreEntity.getName() },
                LocaleContextHolder.getLocale());
        
        CreateStoreResponse response = new CreateStoreResponse(message, storeDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Transactional
    public ResponseEntity<UpdateStoreResponse> updateStore(Long id, UpdateStoreRequest request) {

        Locale locale = LocaleContextHolder.getLocale();
        String notFoundMessage = messageSource.getMessage("error.store.not.found", new Object[] { id }, locale);

        StoreEntity storeEntity = storeRepository.findById(id).orElseThrow(
                () -> new StoreNotFound(notFoundMessage));

        storeMapper.updateEntityFromRequest(request, storeEntity);

        Set<Long> requestedTags = request.getTagsId() == null ? new HashSet<>() : request.getTagsId();
        if (!requestedTags.isEmpty()) {
            validateTags(requestedTags);
        }

        // 1. Eliminar tags desmarcados
        storeEntity.getStoreTags().removeIf(st -> !requestedTags.contains(st.getTagEntity().getId()));

        // 2. Encontrar existentes
        Set<Long> existingTagIds = storeEntity.getStoreTags().stream()
                .map(st -> st.getTagEntity().getId())
                .collect(Collectors.toSet());

        // 3. Agregar nuevos
        for (Long tagId : requestedTags) {
            if (!existingTagIds.contains(tagId)) {
                TagEntity tag = tagRepository.getReferenceById(tagId);
                storeEntity.getStoreTags().add(new StoreTagEntity(storeEntity, tag));
            }
        }

        storeRepository.save(storeEntity);

        // Hallazgo #3: Retorna el objeto DTO completo mapeado
        StoreDto storeDto = storeMapper.toDto(storeEntity);
        String successMessage = messageSource.getMessage("store.updated.success",
                new Object[] { storeEntity.getName() }, locale);

        UpdateStoreResponse response = new UpdateStoreResponse(successMessage, storeDto);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<DeleteStoreResponse> deleteStore(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        StoreEntity store = storeRepository.findById(id).orElseThrow(
                () -> new StoreNotFound(messageSource.getMessage("error.store.not.found", new Object[] { id }, locale)));

        // Soft Delete de la tienda
        store.setDeletedAt(ZonedDateTime.now());
        storeRepository.save(store);

        // Hard Delete de las asociaciones en la tabla pivote
        storeTagRepository.deleteByStoreId(id);

        String message = messageSource.getMessage("store.deleted.success", new Object[] { store.getName() }, locale);
        return ResponseEntity.status(HttpStatus.OK).body(new DeleteStoreResponse(message));
    }

    private void validateTags(Set<Long> tagsId) {
        if (tagsId == null || tagsId.isEmpty()) return;

        List<TagEntity> existingTags = tagRepository.findAllById(tagsId);

        List<TagEntity> activeTags = existingTags.stream()
            .filter(t -> t.getDeletedAt() == null)
            .collect(Collectors.toList());

        if (activeTags.size() != tagsId.size()) {
            Set<Long> foundIds = activeTags.stream()
                .map(TagEntity::getId)
                .collect(Collectors.toSet());
                
            Set<Long> missingIds = new HashSet<>(tagsId);
            missingIds.removeAll(foundIds);
            
            String errorMessage = messageSource.getMessage("error.tags.not.found",
                    new Object[] { missingIds.toString() }, LocaleContextHolder.getLocale());
            throw new TagsNotFound(errorMessage);
        }
    }

    private String getLocalizedOperatingTime(ZonedDateTime createdAt, Locale locale) {
        if (createdAt == null) {
            return messageSource.getMessage("time.unknown", null, locale);
        }

        ZonedDateTime now = ZonedDateTime.now();
        Period period = Period.between(createdAt.toLocalDate(), now.toLocalDate());

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        // Evaluamos Años
        if (years > 0) {
            String key = years == 1 ? "time.years.one" : "time.years.many";
            return messageSource.getMessage(key, new Object[] { years }, locale);
        }
        // Evaluamos Meses
        if (months > 0) {
            String key = months == 1 ? "time.months.one" : "time.months.many";
            return messageSource.getMessage(key, new Object[] { months }, locale);
        }
        // Evaluamos Días
        if (days > 0) {
            String key = days == 1 ? "time.days.one" : "time.days.many";
            return messageSource.getMessage(key, new Object[] { days }, locale);
        }

        // Evaluamos Horas si se creó hoy
        long hours = Duration.between(createdAt, now).toHours();
        if (hours > 0) {
            String key = hours == 1 ? "time.hours.one" : "time.hours.many";
            return messageSource.getMessage(key, new Object[] { hours }, locale);
        }

        // Menos de una hora
        return messageSource.getMessage("time.less.than.hour", null, locale);
    }
}
