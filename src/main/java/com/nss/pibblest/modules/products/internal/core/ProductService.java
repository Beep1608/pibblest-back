package com.nss.pibblest.modules.products.internal.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.products.api.ProductPreviewDto;
import com.nss.pibblest.modules.products.internal.core.exceptions.ProductNotFoundException;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductEntity;
import com.nss.pibblest.modules.products.internal.infrastructure.data.ProductRespository;
import com.nss.pibblest.modules.products.internal.mappers.ProductMapper;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.AssignProductsResponse;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.AssignProductsToStoreRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.createProduct.CreateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.deleteProduct.DeleteProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.deleteProduct.DissociateProductsRequest;
import com.nss.pibblest.modules.products.internal.web.requests.getProductsFromStore.GetProductsFromStoreResponse;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateProductResponse;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateStoreProductQuantitiesRequest;
import com.nss.pibblest.modules.products.internal.web.requests.updateProduct.UpdateStoreProductQuantitiesResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductId;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductRepository;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.tags.api.dto.TagDto;
import com.nss.pibblest.modules.tags.internal.core.exceptions.TagsNotFound;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsEntity;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagForProductsRepository;
import com.nss.pibblest.modules.tags.internal.infrastructure.data.products.TagProductEntity;
import com.nss.pibblest.modules.tags.internal.mappers.TagMapper;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    
    private final ProductRespository productRespository;
    private final StoreProductRepository storeProductRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository; // <-- NUEVA INYECCIÓN
    private final ProductMapper productMapper;
    private final TagMapper tagMapper;
    private final TagForProductsRepository tagForProductsRepository;
    private final MessageSource messageSource;

    public ProductService(ProductRespository productRespository, StoreProductRepository storeProductRepository,
            EmployeeRepository employeeRepository, StoreRepository storeRepository, ProductMapper productMapper, 
            TagMapper tagMapper, TagForProductsRepository tagForProductsRepository, MessageSource messageSource) {
        this.productRespository = productRespository;
        this.storeProductRepository = storeProductRepository;
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.productMapper = productMapper;
        this.tagMapper = tagMapper;
        this.tagForProductsRepository = tagForProductsRepository;
        this.messageSource = messageSource;
    }

    // --- HELPER CENTRALIZADO DE VERIFICACIÓN DE RELACIÓN TIENDA-EMPLEADO ---
    private void verifyStoreAssociationAndPermission(Authentication auth, Long storeId, String moduleAction) {
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        if (isOwner) return;

        UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());
        EmployeeEntity employee = employeeRepository.findById(authenticatedEmployeeId)
                .orElseThrow(() -> new AccessDeniedException("Información de sesión inválida."));

        boolean isAssociated = employee.getEmployeeStores().stream()
                .anyMatch(es -> es.getStore().getId().equals(storeId) && es.isActive());
        
        if (!isAssociated) {
            throw new AccessDeniedException("Operación rechazada: No te encuentras asociado activamente a la sucursal especificada.");
        }

        if (moduleAction != null) {
            String requiredAuthority = "STORE_" + storeId + "_MODULE_PRODUCTS_" + moduleAction.toUpperCase();
            boolean hasPermission = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(requiredAuthority));
            if (!hasPermission) {
                throw new AccessDeniedException("Privilegios insuficientes: Se requiere la autoridad " + requiredAuthority);
            }
        }
    }

    @Transactional
    public ResponseEntity<CreateProductResponse> createProduct(CreateProductRequest request) {
        // Validación de permisos para la creación de un producto maestro en el catálogo
        ProductEntity productRequest = productMapper.toEntity(request);

        // Se arman las asociaciones de tags mientras la entidad sigue siendo transitoria:
        // su colección productTags es aún un ArrayList plano (no un PersistentBag de Hibernate),
        // así que no requiere sesión activa. El CascadeType.ALL las persiste junto al producto.
        if (request.getTagsId() != null && !request.getTagsId().isEmpty()) {
            validateTags(request.getTagsId());
            for (Long tagId : request.getTagsId()) {
                TagForProductsEntity tag = tagForProductsRepository.getReferenceById(tagId);
                productRequest.getProductTags().add(new TagProductEntity(productRequest, tag));
            }
        }

        ProductEntity newProduct = productRespository.save(productRequest);

        CreateProductResponse response = new CreateProductResponse(newProduct.getId(), "Su producto fue registrado");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // REGLA 1: Todos los empleados asignados a una tienda pueden listar sus productos libremente
    public ResponseEntity<GetProductsFromStoreResponse> getProductsFromStore(Long storeId, String keyword, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");
        
        // Verifica que el empleado trabaje en esta tienda (pasa null como acción ya que la regla solo exige asignación)
        verifyStoreAssociationAndPermission(auth, storeId, null);

        Page<StoreProductEntity> storeProductEntities;
        if (keyword == null || keyword.trim().isEmpty()) {
            storeProductEntities = storeProductRepository.findByStoreIdAndIsActiveTrue(storeId, pageable);
        } else {
            storeProductEntities = storeProductRepository
                    .findByStoreIdAndProduct_NameContainingIgnoreCaseAndIsActiveTrue(storeId, keyword, pageable);
        }

        Page<ProductPreviewDto> productEntitys = storeProductEntities.map(spe -> {
            ProductEntity product = spe.getProduct();
            ProductPreviewDto dto = productMapper.toPreviewDto(product);
            dto.setCurrentQuantity(spe.getCurrentQuantity());
            dto.setDesiredQuantity(spe.getDesiredQuantity());

            if (product.getProductTags() != null) {
                List<TagDto> tags = product.getProductTags().stream()
                        .map(tagProduct -> tagMapper.fromTagForProductToDto(tagProduct.getTagForProductsEntity()))
                        .collect(Collectors.toList());
                dto.setTags(tags);
            }
            return dto;
        });
        
        return ResponseEntity.status(HttpStatus.OK).body(new GetProductsFromStoreResponse(productEntitys));
    }

    // REGLA 2: Permite visualizar todo el catálogo global si tiene al menos un permiso READ en el módulo de productos
    public ResponseEntity<GetProductsFromStoreResponse> getAllProducts(String keyword, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");

        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        if (!isOwner) {
            boolean hasReadAnywhere = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().endsWith("_MODULE_PRODUCTS_READ"));
            
            if (!hasReadAnywhere) {
                throw new AccessDeniedException("Acceso denegado: No posees privilegios de lectura global sobre el catálogo maestro.");
            }
        }

        Page<ProductEntity> products;
        if (keyword == null || keyword.trim().isEmpty()) {
            products = productRespository.findByDeletedAtIsNull(pageable);
        } else {
            products = productRespository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(keyword, pageable);
        }

        Page<ProductPreviewDto> productsDto = products.map(productMapper::toPreviewDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GetProductsFromStoreResponse(productsDto));
    }

    public ResponseEntity<ProductPreviewDto> getProductById(Long id) {
        ProductEntity product = productRespository.findByIdWithTags(id)
                .orElseThrow(ProductNotFoundException::new);

        validateProductAccess(product.getId());

        return ResponseEntity.status(HttpStatus.OK).body(productMapper.toPreviewDto(product));
    }

    public ResponseEntity<ProductPreviewDto> getProductByBarcode(String barcode) {
        ProductEntity product = productRespository.findByBarcodeAndDeletedAtIsNull(barcode)
                .orElseThrow(ProductNotFoundException::new);

        validateProductAccess(product.getId());

        return ResponseEntity.status(HttpStatus.OK).body(productMapper.toPreviewDto(product));
    }

    private void validateProductAccess(Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        
        if (!isOwner) {
            // Regla 2: ¿Tiene permiso global de lectura?
            boolean hasGlobalRead = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().endsWith("_MODULE_PRODUCTS_READ"));
            
            if (!hasGlobalRead) {
                // Regla 1: Si no tiene permiso global, validar que el producto exista en al menos una de sus sucursales
                UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());
                EmployeeEntity employee = employeeRepository.findById(authenticatedEmployeeId)
                        .orElseThrow(() -> new AccessDeniedException("No autenticado"));
                
                Set<Long> myStoreIds = employee.getEmployeeStores().stream()
                        .filter(es -> es.isActive())
                        .map(es -> es.getStore().getId())
                        .collect(Collectors.toSet());

                boolean productInMyStores = false;
                for (Long sId : myStoreIds) {
                    StoreProductId spId = new StoreProductId(sId, productId);
                    if (storeProductRepository.findById(spId).filter(sp -> sp.isIsActive()).isPresent()) {
                        productInMyStores = true;
                        break;
                    }
                }
                
                if (!productInMyStores) {
                    throw new AccessDeniedException("No posees los privilegios para visualizar los detalles de este producto.");
                }
            }
        }
    }

    @Transactional
    public ResponseEntity<UpdateProductResponse> editProduct(Long id, UpdateProductRequest request) {
        ProductEntity product = productRespository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(ProductNotFoundException::new);

        productMapper.updateEntityFromRequest(request, product);

        Set<Long> requestedTags = request.getTagsId() == null ? new HashSet<>() : request.getTagsId();
        if (!requestedTags.isEmpty()) {
            validateTags(requestedTags);
        }

        product.getProductTags().removeIf(tagProd -> !requestedTags.contains(tagProd.getTagForProductsEntity().getId()));

        Set<Long> existingTagIds = product.getProductTags().stream()
                .map(tagProd -> tagProd.getTagForProductsEntity().getId())
                .collect(Collectors.toSet());

        for (Long tagId : requestedTags) {
            if (!existingTagIds.contains(tagId)) {
                TagForProductsEntity tag = tagForProductsRepository.getReferenceById(tagId);
                product.getProductTags().add(new TagProductEntity(product, tag));
            }
        }

        product = productRespository.save(product);
        UpdateProductResponse response = new UpdateProductResponse("product.updated.success", productMapper.toPreviewDto(product));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<DeleteProductResponse> deleteProduct(Long id) {
        ProductEntity product = productRespository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(ProductNotFoundException::new);

        product.setDeletedAt(java.time.ZonedDateTime.now());
        productRespository.save(product);

        return ResponseEntity.status(HttpStatus.OK).body(new DeleteProductResponse("product.deleted.success"));
    }

    // REGLA 3: Actualizar stock de productos de tiendas asociadas con verificación estricta de la autoridad UPDATE
    @Transactional
    public ResponseEntity<UpdateStoreProductQuantitiesResponse> updateStoreProductQuantities(Long storeId, Long productId, UpdateStoreProductQuantitiesRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");

        // Valida la asignación activa a la sucursal y la existencia de la autoridad STORE_X_MODULE_PRODUCTS_UPDATE
        verifyStoreAssociationAndPermission(auth, storeId, "UPDATE");

        StoreProductId id = new StoreProductId(storeId, productId);
        StoreProductEntity storeProduct = storeProductRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El producto especificado no se encuentra mapeado en la tienda actual."));

        if (!storeProduct.isIsActive()) {
            throw new IllegalArgumentException("No se pueden alterar cantidades en una relación de inventario inactiva.");
        }

        storeProduct.setDesiredQuantity(request.desiredQuantity());
        storeProduct.setCurrentQuantity(request.currentQuantity());
        storeProductRepository.save(storeProduct);

        String msg = messageSource.getMessage("product.store.quantities.updated", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(new UpdateStoreProductQuantitiesResponse(msg));
    }

    // REGLA 4: Eliminar de forma masiva (n) asociaciones con una tienda comprobando la asignación física y la autoridad DELETE
    @Transactional
    public ResponseEntity<DeleteProductResponse> dissociateProductsFromStore(Long storeId, DissociateProductsRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");

        // Valida la asignación activa a la sucursal y la existencia de la autoridad STORE_X_MODULE_PRODUCTS_DELETE
        verifyStoreAssociationAndPermission(auth, storeId, "DELETE");

        List<StoreProductEntity> associations = storeProductRepository.findByStoreIdAndProductIdIn(storeId, request.productIds());
        
        for (StoreProductEntity storeProduct : associations) {
            storeProduct.setIsActive(false); // Baja lógica de la relación simétrica
        }
        storeProductRepository.saveAll(associations);

        String msg = messageSource.getMessage("product.store.dissociation.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(new DeleteProductResponse(msg));
    }

    // REGLA 5: Asociar masivamente n productos a una tienda
    @Transactional
    public ResponseEntity<AssignProductsResponse> assignProductsToStore(Long storeId, AssignProductsToStoreRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");

        // Validamos la asignación física y exigimos la autoridad CREATE sobre el módulo PRODUCTS
        verifyStoreAssociationAndPermission(auth, storeId, "CREATE");

        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("La sucursal especificada no existe."));

        for (AssignProductsToStoreRequest.ProductAssignmentItem item : request.items()) {
            ProductEntity product = productRespository.findByIdAndDeletedAtIsNull(item.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto con ID " + item.productId() + " no encontrado o no disponible."));

            // Verificamos stock global
            if (product.getQuantity() < item.quantity()) {
                throw new IllegalArgumentException("Inventario global insuficiente para el producto [" + product.getName() + "]. " +
                        "Solicitado: " + item.quantity() + ", Disponible: " + product.getQuantity());
            }

            // Descontamos del catálogo general maestro
            product.setQuantity(product.getQuantity() - item.quantity());
            productRespository.save(product);

            // Asignamos o acumulamos en la tabla pivote de la tienda
            StoreProductId spId = new StoreProductId(storeId, item.productId());
            StoreProductEntity storeProduct = storeProductRepository.findById(spId)
                    .orElseGet(() -> {
                        StoreProductEntity newSp = new StoreProductEntity(store, product);
                        newSp.setDesiredQuantity(0L);
                        newSp.setCurrentQuantity(0L);
                        return newSp;
                    });

            storeProduct.setDesiredQuantity(storeProduct.getDesiredQuantity() + item.quantity());
            storeProduct.setCurrentQuantity(storeProduct.getCurrentQuantity() + item.quantity());
            storeProduct.setIsActive(true); // Reactivamos en caso de que hubiera un Soft Delete previo

            storeProductRepository.save(storeProduct);
        }

        String msg = messageSource.getMessage("product.store.assignment.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(new AssignProductsResponse(msg));
    }

    private void validateTags(Set<Long> tagsId) {
        if (tagsId == null || tagsId.isEmpty()) return;

        List<TagForProductsEntity> existingTags = tagForProductsRepository.findAllById(tagsId);

        List<TagForProductsEntity> activeTags = existingTags.stream()
            .filter(t -> t.getDeletedAt() == null)
            .collect(Collectors.toList());

        if (activeTags.size() != tagsId.size()) {
            Set<Long> foundIds = activeTags.stream()
                .map(TagForProductsEntity::getId)
                .collect(Collectors.toSet());
                
            Set<Long> missingIds = new HashSet<>(tagsId);
            missingIds.removeAll(foundIds);
            
            String errorMessage = messageSource.getMessage("error.tags.not.found",
                    new Object[] { missingIds.toString() }, LocaleContextHolder.getLocale());
            throw new TagsNotFound(errorMessage);
        }
    }
}
