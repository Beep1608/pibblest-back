package com.nss.pibblest.modules.stores.internal.web;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.stores.api.dtos.StoreSimpleDto;
import com.nss.pibblest.modules.stores.internal.core.StoreService;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.deleteStore.DeleteStoreResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.getAllStores.GetAllStoresResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stores")
@Tag(name = "Stores", description = "Endpoints para la gestión, búsqueda y administración maestra de tiendas")
public class StoreController {
    
    private final StoreService storeService;

    public StoreController(StoreService storeService){
        this.storeService = storeService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Obtener todas las tiendas (Paginado)")
    public ResponseEntity<GetAllStoresResponse> getAllStores(Pageable pageable){
        return storeService.getAllStores(pageable);
    }

    @GetMapping("/simple")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Obtener listado simple de tiendas")
    public ResponseEntity<List<StoreSimpleDto>> getAllStoresSimple() {
        return storeService.getAllStoresSimple();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Buscar tiendas por palabra clave")
    public ResponseEntity<GetAllStoresResponse> searchStores(
            @Parameter(description = "Palabra clave para buscar") @RequestParam("keyword") String keyword, 
            Pageable pageable) {
        return storeService.searchStores(keyword, pageable);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('OWNER')") 
    @Operation(summary = "Crear una nueva tienda")
    public ResponseEntity<CreateStoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request) {
        return storeService.createStore(request);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('OWNER')") 
    @Operation(summary = "Actualizar información de una tienda")
    public ResponseEntity<UpdateStoreResponse> updateStore(
            @PathVariable("id") Long id, 
            @Valid @RequestBody UpdateStoreRequest request) {
        return storeService.updateStore(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')") 
    @Operation(summary = "Eliminar una tienda (Soft Delete)")
    public ResponseEntity<DeleteStoreResponse> deleteStore(@PathVariable("id") Long id) {
        return storeService.deleteStore(id);
    }
}