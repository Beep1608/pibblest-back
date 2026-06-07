package com.nss.pibblest.modules.sales.internal.core;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.nss.pibblest.modules.notifications.internal.core.store.StoreNotificationHelper;
import com.nss.pibblest.modules.sales.api.dto.SaleDto;
import com.nss.pibblest.modules.sales.internal.core.exceptions.SaleValidationException;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleDetailEntity;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleEntity;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleRepository;
import com.nss.pibblest.modules.sales.internal.mappers.SaleMapper;
import com.nss.pibblest.modules.sales.internal.web.requests.cancelSale.CancelSaleResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleRequest;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.getSales.GetSalesResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductId;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreProductRepository;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.shared.exceptions.EntityNotFoundException;

import jakarta.transaction.Transactional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final StoreRepository storeRepository;
    private final StoreProductRepository storeProductRepository;
    private final StoreNotificationHelper storeNotificationHelper;
    private final MessageSource messageSource;
    private final SaleMapper saleMapper;
    private final EmployeeRepository employeeRepository;

    public SaleService(SaleRepository saleRepository, StoreRepository storeRepository,
            StoreProductRepository storeProductRepository,
            StoreNotificationHelper storeNotificationHelper, MessageSource messageSource,
            SaleMapper saleMapper, EmployeeRepository employeeRepository) {
        this.saleRepository = saleRepository;
        this.storeRepository = storeRepository;
        this.storeProductRepository = storeProductRepository;
        this.storeNotificationHelper = storeNotificationHelper;
        this.messageSource = messageSource;
        this.saleMapper = saleMapper;
        this.employeeRepository = employeeRepository;
    }

    // --- HELPER DE SEGURIDAD ---
    private void verifyStorePermission(Long storeId, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("No autenticado");

        boolean isOwner = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        if (isOwner) return;

        String requiredAuthority = "STORE_" + storeId + "_MODULE_SALES_" + action;
        boolean hasPerm = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(requiredAuthority));

        if (!hasPerm) {
            throw new AccessDeniedException("No tienes permisos de " + action + " sobre el módulo de ventas en esta tienda.");
        }
    }
    // ---------------------------

    @Transactional
    public ResponseEntity<CreateSaleResponse> createSale(CreateSaleRequest request) {
        Locale locale = LocaleContextHolder.getLocale();

        // 1. Obtener los datos del empleado autenticado desde el JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());

        // 2. Validar que la tienda exista y esté activa
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("store.not.found", null, locale)));

        if (!"ACTIVE".equalsIgnoreCase(store.getStatus())) {
            throw new SaleValidationException(messageSource.getMessage("store.inactive", null, locale));
        }

        // 3. NUEVA VALIDACIÓN: Verificar que el empleado esté legítimamente asociado a la tienda y activo
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        
        // Obtenemos la entidad real del empleado para poder leer sus tiendas (y de paso lo usamos para guardar la venta)
        EmployeeEntity authEmployee = 
                employeeRepository.findById(authenticatedEmployeeId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Empleado no encontrado"));

        if (!isOwner) {
            boolean isAssociated = authEmployee.getEmployeeStores().stream()
                    .anyMatch(es -> es.getStore().getId().equals(request.getStoreId()) && es.isActive());
            
            if (!isAssociated) {
                throw new org.springframework.security.access.AccessDeniedException("No estás asociado a esta tienda para realizar operaciones.");
            }
        }

        // 4. Preparar la entidad Venta e inyectar el Empleado
        SaleEntity sale = new SaleEntity();
        sale.setStore(store);
        sale.setEmployee(authEmployee); // Ya tenemos la entidad completa, la inyectamos directamente
        
        BigDecimal totalSaleAmount = BigDecimal.ZERO;

        // --- (Tu lógica existente de control de stock permanece igual) ---
        List<Long> productsIds = request.getItems().stream().map(CreateSaleRequest.SaleItemRequest::getProductId).toList();
        List<StoreProductEntity> localInventory = storeProductRepository.findByStoreIdAndProductIdIn(request.getStoreId(), productsIds);
        Map<Long, StoreProductEntity> inventoryMap = localInventory.stream().collect(Collectors.toMap(sp -> sp.getProduct().getId(), sp -> sp));

        for (CreateSaleRequest.SaleItemRequest item : request.getItems()) {
            Long productId = item.getProductId();
            Integer requestedQuantity = item.getQuantity();

            StoreProductEntity storeProduct = inventoryMap.get(productId);
            if (storeProduct == null) {
                throw new EntityNotFoundException(messageSource.getMessage("products.not.found", new Object[] { productId }, locale));
            }
            if (!storeProduct.isIsActive()) {
                throw new SaleValidationException(messageSource.getMessage("products.inactive", new Object[] { storeProduct.getProduct().getName() }, locale));
            }
            if (storeProduct.getCurrentQuantity() < requestedQuantity) {
                throw new SaleValidationException(messageSource.getMessage("inventory.not.suficient.stock", new Object[] { storeProduct.getProduct().getName(), requestedQuantity, storeProduct.getCurrentQuantity() }, locale));
            }

            storeProduct.setCurrentQuantity(storeProduct.getCurrentQuantity() - requestedQuantity);
            BigDecimal unitPrice = storeProduct.getProduct().getBasePrice();

            SaleDetailEntity detail = new SaleDetailEntity(sale, storeProduct.getProduct(), requestedQuantity, unitPrice);
            sale.addDetail(detail);
            totalSaleAmount = totalSaleAmount.add(detail.getSubtotal());
        }

        sale.setTotalAmount(totalSaleAmount);
        sale.setStatus("COMPLETED");

        SaleEntity savedSale = saleRepository.save(sale);

        storeNotificationHelper.notifyStoreChange(store.getId(), (String) auth.getPrincipal());
        CreateSaleResponse response = new CreateSaleResponse(messageSource.getMessage("sales.done", null, locale));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // LISTADO FILTRADO POR AMBOS REQUISITOS (Scope ALL vs PERSONAL)
    public ResponseEntity<GetSalesResponse> getSalesByStore(Long storeId, String scope, java.util.UUID targetEmployeeId, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID authenticatedEmployeeId = UUID.fromString((String) auth.getPrincipal());
        
        boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        
        // REQUISITO 1: Si no es Owner, el empleado DEBE estar asociado de forma activa a la tienda obligatoriamente
        if (!isOwner) {
            com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity authEmployee = 
                    employeeRepository.findById(authenticatedEmployeeId)
                    .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Empleado no encontrado"));
            
            boolean isAssociated = authEmployee.getEmployeeStores().stream()
                    .anyMatch(es -> es.getStore().getId().equals(storeId) && es.isActive());
            
            if (!isAssociated) {
                throw new AccessDeniedException("No tienes acceso a los registros de esta tienda.");
            }
        }

        Page<SaleEntity> salesPage;

        // SOLUCIÓN AL BUG: Evaluamos estrictamente si el scope solicitado es ALL
        if ("ALL".equalsIgnoreCase(scope)) {
            
            // REQUISITO 2: Si es un empleado, validar que tenga la autoridad explícita de READ para esta tienda
            if (!isOwner) {
                String requiredAuthority = "STORE_" + storeId + "_MODULE_SALES_READ";
                boolean hasReadPermission = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(requiredAuthority));
                
                if (!hasReadPermission) {
                    throw new AccessDeniedException("No tienes privilegios para visualizar todas las ventas de esta tienda.");
                }
            }

            // NUEVA FUNCIONALIDAD: Si el alcance es ALL y se proporciona un employeeId, filtramos por ese usuario específico
            if (targetEmployeeId != null) {
                salesPage = saleRepository.findByStoreIdAndEmployeeIdAndDeletedAtIsNull(storeId, targetEmployeeId, pageable);
            } else {
                // Si no se envía un filtro de usuario, muestra todo el historial general de la tienda
                salesPage = saleRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable);
            }
            
        } else {
            // SI EL SCOPE ES "PERSONAL" (Tanto para OWNER como para EMPLOYEE)
            // Se fuerza el filtro utilizando el ID extraído directamente del token JWT autenticado
            salesPage = saleRepository.findByStoreIdAndEmployeeIdAndDeletedAtIsNull(storeId, authenticatedEmployeeId, pageable);
        }

        Page<SaleDto> dtoPage = salesPage.map(saleMapper::toDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GetSalesResponse(dtoPage));
    }

    public ResponseEntity<SaleDto> getSaleById(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        SaleEntity sale = saleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("sales.not.found", new Object[]{id}, locale)));
        
        verifyStorePermission(sale.getStore().getId(), "READ");
        
        return ResponseEntity.status(HttpStatus.OK).body(saleMapper.toDto(sale));
    }

    @Transactional
    public ResponseEntity<CancelSaleResponse> cancelSale(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        SaleEntity sale = saleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("sales.not.found", new Object[]{id}, locale)));

        verifyStorePermission(sale.getStore().getId(), "UPDATE");

        if ("CANCELLED".equalsIgnoreCase(sale.getStatus())) {
            throw new SaleValidationException(messageSource.getMessage("sales.already.cancelled", null, locale));
        }

        sale.setStatus("CANCELLED");

        for (SaleDetailEntity detail : sale.getDetails()) {
            StoreProductId spId = new StoreProductId(sale.getStore().getId(), detail.getProduct().getId());
            storeProductRepository.findById(spId).ifPresent(sp -> {
                sp.setCurrentQuantity(sp.getCurrentQuantity() + detail.getQuantity());
                storeProductRepository.save(sp);
            });
        }

        saleRepository.save(sale);
        
        storeNotificationHelper.notifyStoreChange(sale.getStore().getId(),
                (String) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()));

        CancelSaleResponse response = new CancelSaleResponse(messageSource.getMessage("sales.cancelled.success", null, locale));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<Void> deleteSale(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        SaleEntity sale = saleRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("sales.not.found", new Object[]{id}, locale)));

        verifyStorePermission(sale.getStore().getId(), "DELETE");

        sale.setDeletedAt(ZonedDateTime.now());
        saleRepository.save(sale);

        return ResponseEntity.noContent().build();
    }
}
