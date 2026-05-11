package com.nss.pibblest.modules.stores.internal.core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductId;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductRepository;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.stores.internal.web.requests.storeProducts.asignProductToStore.AsignStoreProductToStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.storeProducts.asignProductToStore.AsignStoreProductToStoreResponse;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class StoreProductService {

    private final StoreProductRepository storeProductRepository;
    private final StoreRepository storeRepository;
    private final ProductRespository productRespository;

    public StoreProductService(StoreProductRepository storeProductRepository,
        StoreRepository storeRepository, ProductRespository productRespository
    ){
        this.storeProductRepository = storeProductRepository;
        this.storeRepository = storeRepository;
        this.productRespository = productRespository;
    }

    @Transactional
    public ResponseEntity<AsignStoreProductToStoreResponse> assignProductToStore(AsignStoreProductToStoreRequest request){

        ProductEntity productEntity = productRespository.findById(request.getProductId())
        .orElseThrow(() -> new EntityNotFoundException("No se encontró el producto con id: "+ request.getProductId()));

        StoreEntity storeEntity = storeRepository.findById(request.getStoreId())
        .orElseThrow(() -> new EntityNotFoundException("No se encontró la tienda con id: "+ request.getStoreId()));

        if(productEntity.getQuantity() < request.getQuantity()){
            throw new IllegalStateException("El stock es menor a la cantidad que se queire asignar");
        }

        productEntity.setQuantity(productEntity.getQuantity() - request.getQuantity());
        productRespository.save(productEntity);

        StoreProductId storeProductId = new StoreProductId(request.getStoreId(), request.getProductId());

        StoreProductEntity storeProductEntity = storeProductRepository.findById(storeProductId)
        .orElseGet(() -> {
            StoreProductEntity newStoreProductEntity = new StoreProductEntity();
            newStoreProductEntity.setId(storeProductId);
            
        

            newStoreProductEntity.setProduct(productEntity);
            newStoreProductEntity.setStore(storeEntity);
            newStoreProductEntity.setDesiredQuantity(request.getQuantity());
            return newStoreProductEntity;
        });

        storeProductEntity.setDesiredQuantity(request.getQuantity());
        storeProductEntity.setCurrentQuantity(request.getQuantity());
        storeProductEntity.setIsActive(true);

        storeProductRepository.save(storeProductEntity);

        AsignStoreProductToStoreResponse response = new AsignStoreProductToStoreResponse("Se asigno correctamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


}
