package com.nss.pibblest.modules.stores.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.stores.internal.core.StoreService;
import com.nss.pibblest.modules.stores.internal.web.requests.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.createStore.CreateStoreResponse;


import jakarta.validation.Valid;
//TODO: Revisar le manejo de responses correctamente para este modulo
@RestController
@RequestMapping("/api/stores")
public class StoreController {
    
    private final StoreService storeService;

    public StoreController(StoreService storeService){
        this.storeService = storeService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateStoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request){
        return storeService.createStore(request);
    }

}
