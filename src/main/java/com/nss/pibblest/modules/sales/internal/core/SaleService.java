package com.nss.pibblest.modules.sales.internal.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public SaleService(SaleRepository saleRepository, StoreRepository storeRepository,
            StoreProductRepository storeProductRepository,
            StoreNotificationHelper storeNotificationHelper, MessageSource messageSource,
            SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.storeRepository = storeRepository;
        this.storeProductRepository = storeProductRepository;
        this.storeNotificationHelper = storeNotificationHelper;
        this.messageSource = messageSource;
        this.saleMapper = saleMapper;
    }

    @Transactional
    public ResponseEntity<CreateSaleResponse> createSale(CreateSaleRequest request) {

        Locale locale = LocaleContextHolder.getLocale();

        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(
                        () -> new EntityNotFoundException(messageSource.getMessage("store.not.found", null, locale)));

        if (!"ACTIVE".equalsIgnoreCase(store.getStatus())) {
            throw new SaleValidationException(messageSource.getMessage("store.inactive", null, locale));
        }

        List<Long> productsIds = request.getItems().stream()
                .map(CreateSaleRequest.SaleItemRequest::getProductId)
                .toList();

        List<StoreProductEntity> localInventory = storeProductRepository
                .findByStoreIdAndProductIdIn(request.getStoreId(), productsIds);

        Map<Long, StoreProductEntity> inventoryMap = localInventory.stream()
                .collect(Collectors.toMap(sp -> sp.getProduct().getId(), sp -> sp));

        SaleEntity sale = new SaleEntity();
        sale.setStore(store);
        BigDecimal totalSaleAmount = BigDecimal.ZERO;

        for (CreateSaleRequest.SaleItemRequest item : request.getItems()) {

            Long productId = item.getProductId();
            Integer requestedQuantity = item.getQuantity();

            StoreProductEntity storeProduct = inventoryMap.get(productId);
            if (storeProduct == null) {
                throw new EntityNotFoundException(
                        messageSource.getMessage("products.not.found", new Object[] { productId }, locale));
            }

            if (!storeProduct.isIsActive()) {
                throw new SaleValidationException(messageSource.getMessage("products.inactive",
                        new Object[] { storeProduct.getProduct().getName() }, locale));
            }

            if (storeProduct.getCurrentQuantity() < requestedQuantity) {
                throw new SaleValidationException(messageSource.getMessage("inventory.not.suficient.stock",
                        new Object[] { storeProduct.getProduct().getName(),
                                requestedQuantity,
                                storeProduct.getCurrentQuantity() },
                        locale));
            }

            storeProduct.setCurrentQuantity(storeProduct.getCurrentQuantity() - requestedQuantity);
            BigDecimal unitPrice = storeProduct.getProduct().getBasePrice();

            SaleDetailEntity detail = new SaleDetailEntity(sale, storeProduct.getProduct(), requestedQuantity,
                    unitPrice);

            sale.addDetail(detail);
            totalSaleAmount = totalSaleAmount.add(detail.getSubtotal());
        }

        sale.setTotalAmount(totalSaleAmount);
        sale.setStatus("COMPLETED");

        SaleEntity savedSale = saleRepository.save(sale);

        storeNotificationHelper.notifyStoreChange(store.getId(),
                (String) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()));

        CreateSaleResponse response = new CreateSaleResponse(messageSource.getMessage("sales.done", null, locale));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<GetSalesResponse> getSalesByStore(Long storeId, Pageable pageable) {
        Page<SaleEntity> salesPage = saleRepository.findByStoreId(storeId, pageable);
        Page<SaleDto> dtoPage = salesPage.map(saleMapper::toDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GetSalesResponse(dtoPage));
    }

    public ResponseEntity<SaleDto> getSaleById(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("sales.not.found", new Object[]{id}, locale)));
        return ResponseEntity.status(HttpStatus.OK).body(saleMapper.toDto(sale));
    }

    @Transactional
    public ResponseEntity<CancelSaleResponse> cancelSale(Long id) {
        Locale locale = LocaleContextHolder.getLocale();
        SaleEntity sale = saleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("sales.not.found", new Object[]{id}, locale)));

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
}
