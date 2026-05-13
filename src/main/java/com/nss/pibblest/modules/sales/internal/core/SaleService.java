package com.nss.pibblest.modules.sales.internal.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.notifications.internal.core.store.StoreNotificationHelper;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleDetailEntity;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleEntity;
import com.nss.pibblest.modules.sales.internal.infrastructure.data.SaleRepository;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleRequest;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleResponse;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
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

    public SaleService(SaleRepository saleRepository, StoreRepository storeRepository,
            StoreProductRepository storeProductRepository,
            StoreNotificationHelper storeNotificationHelper, MessageSource messageSource) {
        this.saleRepository = saleRepository;
        this.storeRepository = storeRepository;
        this.storeProductRepository = storeProductRepository;
        this.storeNotificationHelper = storeNotificationHelper;
        this.messageSource = messageSource;
    }

    @Transactional
    public ResponseEntity<CreateSaleResponse> createSale(CreateSaleRequest request) {

        Locale locale = LocaleContextHolder.getLocale();

        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(
                        () -> new EntityNotFoundException(messageSource.getMessage("store.not.found", null, locale)));

        if (!"ACTIVE".equalsIgnoreCase(store.getStatus())) {
            throw new IllegalStateException(messageSource.getMessage("store.inactive", null, locale));
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
                throw new IllegalStateException(messageSource.getMessage("products.inactive",
                        new Object[] { storeProduct.getProduct().getName() }, locale));
            }

            if (storeProduct.getCurrentQuantity() < requestedQuantity) {
                throw new IllegalStateException(messageSource.getMessage("inventory.not.suficient.stock",
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

        //TODO: Cuando se notifica de un estadi INIT -> STORE_UPDATE arroja un access denied pero sigue streameando
        storeNotificationHelper.notifyStoreChange(store.getId(),
                (String) (SecurityContextHolder.getContext().getAuthentication().getPrincipal()));

        CreateSaleResponse response = new CreateSaleResponse(messageSource.getMessage("sales.done", null, locale));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
