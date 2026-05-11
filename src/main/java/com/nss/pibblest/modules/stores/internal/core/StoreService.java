package com.nss.pibblest.modules.stores.internal.core;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;
import com.nss.pibblest.modules.stores.internal.core.exceptions.StoreNotFound;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.stores.internal.mappers.StoreMapper;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.getAllStores.GetAllStoresResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreResponse;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final MessageSource messageSource;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, MessageSource messageSource) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.messageSource = messageSource;
    }

    public ResponseEntity<GetAllStoresResponse> getAllStores(Pageable pageable) {

        ZonedDateTime startOfToday = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Page<StorePreviewDto> storesPage = storeRepository.findStorePreviewInfo(pageable, startOfToday);

        
        GetAllStoresResponse response = new GetAllStoresResponse(storesPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<CreateStoreResponse> createStore(CreateStoreRequest request) {

        StoreEntity storeEntity = storeMapper.toEntity(request);

        StoreEntity newStoreEntity = storeRepository.save(storeEntity);

        String message = messageSource.getMessage("store.created.response", new Object[] { newStoreEntity.getName() },
                LocaleContextHolder.getLocale());
        CreateStoreResponse response = new CreateStoreResponse(message);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<UpdateStoreResponse> updateStore(Long id, UpdateStoreRequest request) {

        Locale locale = LocaleContextHolder.getLocale();
        String notFoundMessage = messageSource.getMessage("error.store.not.found", new Object[] { id }, locale);

        StoreEntity storeEntity = storeRepository.findById(id).orElseThrow(
                () -> new StoreNotFound(notFoundMessage));

        storeMapper.updateEntityFromRequest(request, storeEntity);

        storeRepository.save(storeEntity);

        String successMessage = messageSource.getMessage("store.updated.success",
                new Object[] { storeEntity.getName() }, locale);

        UpdateStoreResponse response = new UpdateStoreResponse(successMessage);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
