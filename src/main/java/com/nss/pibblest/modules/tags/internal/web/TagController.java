package com.nss.pibblest.modules.tags.internal.web;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.tags.internal.core.TagService;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.assignTag.AssignTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.createGroup.CreateTagResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.getAllTags.GetAllTagsResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct.AssignTagToProductRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.products.assignTagToProduct.AssignTagToProductResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct.CreateTagForProductRequest;
import com.nss.pibblest.modules.tags.internal.web.requests.products.createTagForProduct.CreateTagForProductResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.getAllTagsList.GetAllStoreTagsListResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.getAllProductTagsList.GetAllProductTagsListResponse;
import com.nss.pibblest.modules.tags.internal.web.requests.products.getAllTagsForProducts.GetAllTagsForProductsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tags & Clasificaciones", description = "Endpoints para la gestión del catálogo de Tags (tanto para Tiendas como para Productos).")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // ==========================================
    // SECCIÓN: TAGS DE TIENDAS
    // ==========================================

    @PostMapping("/create")
    @Operation(summary = "Crear Tag de Tienda", description = "Crea un nuevo tag general para clasificar tiendas.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tag creado exitosamente")
    })
    public ResponseEntity<CreateTagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        return tagService.createTag(request);
    }

    @PostMapping("/assign")
    @Operation(summary = "Asignar Tags a una Tienda", description = "Vincula un conjunto de tags existentes a una tienda específica.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Tags asignados correctamente"),
        @ApiResponse(responseCode = "404", description = "Tienda o algún Tag no encontrado")
    })
    public ResponseEntity<AssignTagResponse> assignTagToStore(@Valid @RequestBody AssignTagRequest request) {
        return tagService.assignTagToStore(request);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar Tags de Tiendas (Paginado)", description = "Devuelve el listado de tags de tiendas (excluyendo borrados lógicos) junto con la cantidad de veces que se ha usado cada uno.")
    public ResponseEntity<GetAllTagsResponse> getAllTags(Pageable pageable) {
        return tagService.getAllTags(pageable);
    }

    @GetMapping("/stores/list")
    @Operation(summary = "Listado simple de Tags de Tiendas", description = "Retorna todos los tags de tiendas sin paginación.")
    public ResponseEntity<GetAllStoreTagsListResponse> getAllStoreTagsList() {
        return tagService.getAllStoreTagsList();
    }

    @PutMapping("/edit/{id}")
    @Operation(summary = "Editar Tag de Tienda", description = "Actualiza el nombre de un tag de tienda existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Tag no encontrado")
    })
    public ResponseEntity<AssignTagResponse> updateTag(
            @Parameter(description = "ID del tag a editar") @PathVariable("id") Long id, 
            @Valid @RequestBody CreateTagRequest request) {
        return tagService.updateTag(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Tag de Tienda", description = "Realiza un borrado lógico del tag de tienda para no romper relaciones previas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Tag no encontrado")
    })
    public ResponseEntity<AssignTagResponse> deleteTag(
            @Parameter(description = "ID del tag a eliminar") @PathVariable("id") Long id) {
        return tagService.deleteTag(id);
    }

    // ==========================================
    // SECCIÓN: TAGS DE PRODUCTOS
    // ==========================================

    @PostMapping("/create-to-products")
    @Operation(summary = "Crear Tag de Producto", description = "Crea un nuevo tag destinado exclusivamente a la clasificación de productos.")
    @ApiResponse(responseCode = "201", description = "Tag de producto creado exitosamente")
    public ResponseEntity<CreateTagForProductResponse> createTagToProduct(@Valid @RequestBody CreateTagForProductRequest request){
        return tagService.createTagToProduct(request);
    }

    @PostMapping("/assign-to-product")
    @Operation(summary = "Asignar Tags a un Producto", description = "Vincula un conjunto de tags de productos existentes a un producto en específico.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tags asignados al producto correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto o algún Tag no encontrado")
    })
    public ResponseEntity<AssignTagToProductResponse> assignTagToProduct(
            @Valid @RequestBody AssignTagToProductRequest request) {
        return tagService.assignTagToProduct(request);
    }

    @GetMapping("/get-all-products")
    @Operation(summary = "Buscar/Listar Tags de Productos (Paginado)", description = "Devuelve el listado de tags de productos con su respectivo contador de uso. Si se provee la 'keyword', filtrará por coincidencias en el nombre.")
    public ResponseEntity<GetAllTagsForProductsResponse> getAllTagsForProducts(
            @Parameter(description = "Palabra clave para filtrar por nombre (opcional)") @RequestParam(required = false) String keyword, 
            Pageable pageable){
        return  tagService.getAllTagsForProducts(keyword, pageable);
    } 

    @GetMapping("/products/list")
    @Operation(summary = "Listado simple de Tags de Productos", description = "Retorna todos los tags de productos sin paginación.")
    public ResponseEntity<GetAllProductTagsListResponse> getAllProductTagsList() {
        return tagService.getAllProductTagsList();
    }

    @PutMapping("/products/edit/{id}")
    @Operation(summary = "Editar Tag de Producto", description = "Actualiza el nombre de un tag de producto existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag de producto actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Tag de producto no encontrado")
    })
    public ResponseEntity<AssignTagToProductResponse> updateTagToProduct(
            @Parameter(description = "ID del tag de producto a editar") @PathVariable("id") Long id, 
            @Valid @RequestBody CreateTagForProductRequest request) {
        return tagService.updateTagToProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Eliminar Tag de Producto", description = "Realiza un borrado lógico del tag de producto para no afectar el histórico de los productos ya etiquetados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag de producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Tag de producto no encontrado")
    })
    public ResponseEntity<AssignTagToProductResponse> deleteTagToProduct(
            @Parameter(description = "ID del tag de producto a eliminar") @PathVariable("id") Long id) {
        return tagService.deleteTagToProduct(id);
    }
}
