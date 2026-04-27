package com.nss.pibblest.modules.stores.internal.core;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.stores.internal.mappers.StoreMapper;
import com.nss.pibblest.modules.stores.internal.web.requests.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.createStore.CreateStoreResponse;

@Service
public class StoreService {
    
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final MessageSource messageSource;


    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, MessageSource messageSource){
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.messageSource = messageSource;
    }

    public ResponseEntity<CreateStoreResponse> createStore (CreateStoreRequest request){

        StoreEntity storeEntity = storeMapper.toEntity(request);

        StoreEntity newStoreEntity = storeRepository.save(storeEntity);

        String message = messageSource.getMessage("store.created.respose", new Object[] { newStoreEntity.getName()},
        LocaleContextHolder.getLocale());
        CreateStoreResponse response = new CreateStoreResponse(message);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
