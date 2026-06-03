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
import com.nss.pibblest.modules.stores.internal.core.StoreProductService;
import com.nss.pibblest.modules.stores.internal.core.StoreService;
import com.nss.pibblest.modules.stores.internal.web.requests.storeProducts.asignProductToStore.AsignStoreProductToStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.storeProducts.asignProductToStore.AsignStoreProductToStoreResponse;
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
@Tag(name = "Stores", description = "Endpoints para la gestión, búsqueda y asignación de tiendas")
public class StoreController {
    
    private final StoreService storeService;
    private final StoreProductService storeProductService;

    public StoreController(StoreService storeService, StoreProductService storeProductService){
        this.storeService = storeService;
        this.storeProductService = storeProductService;
    }

    @GetMapping("/all")
    @Operation(
        summary = "Obtener todas las tiendas (Paginado)", 
        description = "Retorna un listado paginado de todas las tiendas con su información de previsualización y KPIs calculados al día de hoy."
    )
    @ApiResponse(responseCode = "200", description = "Listado de tiendas obtenido exitosamente")
    public ResponseEntity<GetAllStoresResponse> getAllStores(Pageable pageable){
        return storeService.getAllStores(pageable);
    }

    @GetMapping("/simple")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Obtener listado simple de tiendas", 
        description = "Retorna un listado sin paginar que contiene exclusivamente el ID y Nombre de las tiendas activas. Exclusivo para usuarios con rol OWNER."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado simple obtenido exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Rol insuficiente)")
    })
    public ResponseEntity<List<StoreSimpleDto>> getAllStoresSimple() {
        return storeService.getAllStoresSimple();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "Buscar tiendas por palabra clave", 
        description = "Filtra las tiendas cuyo nombre coincida (parcialmente) con la keyword proporcionada. Incluye los tags asociados y devuelve resultados paginados. Exclusivo para rol OWNER."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (Rol insuficiente)")
    })
    public ResponseEntity<GetAllStoresResponse> searchStores(
            @Parameter(description = "Palabra clave para buscar en el nombre de la tienda", example = "Sucursal Norte") 
            @RequestParam("keyword") String keyword, 
            Pageable pageable) {
        return storeService.searchStores(keyword, pageable);
    }

    @PostMapping("/create")
    @Operation(
        summary = "Crear nueva tienda", 
        description = "Crea una nueva tienda en el sistema. Opcionalmente recibe una lista de IDs de tags para asociarlos inmediatamente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tienda creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Errores de validación en los datos de entrada"),
        @ApiResponse(responseCode = "404", description = "Alguno de los Tags proporcionados no existe")
    })
    public ResponseEntity<CreateStoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request){
        return storeService.createStore(request);
    }

    @PutMapping("/edit/{id}")
    @Operation(
        summary = "Actualizar tienda", 
        description = "Actualiza los datos básicos de la tienda y sincroniza sus tags (agrega nuevos y elimina los omitidos)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tienda actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tienda o Tags no encontrados")
    })
    public ResponseEntity<UpdateStoreResponse> updateStore (
            @Parameter(description = "ID de la tienda a actualizar", example = "1") 
            @PathVariable("id") Long id, 
            @Valid @RequestBody UpdateStoreRequest request){
        return storeService.updateStore(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar tienda", 
        description = "Realiza un borrado lógico (soft delete) de la tienda especificada y elimina físicamente (hard delete) sus relaciones en la tabla de tags."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tienda eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tienda no encontrada")
    })
    public ResponseEntity<DeleteStoreResponse> deleteStore(
            @Parameter(description = "ID de la tienda a eliminar", example = "1") 
            @PathVariable("id") Long id) {
        return storeService.deleteStore(id);
    }

    @PostMapping("/assign")
    @Operation(
        summary = "Asignar producto a tienda", 
        description = "Transfiere un producto existente del inventario global hacia una tienda específica estableciendo una cantidad inicial/deseada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto asignado a la tienda exitosamente"),
        @ApiResponse(responseCode = "400", description = "Inventario insuficiente o errores de validación"),
        @ApiResponse(responseCode = "404", description = "Producto o Tienda no encontrados")
    })
    public ResponseEntity<AsignStoreProductToStoreResponse> assignProduct (@Valid @RequestBody AsignStoreProductToStoreRequest request){
        return this.storeProductService.assignProductToStore(request);
    }
}