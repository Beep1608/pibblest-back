package com.nss.pibblest.modules.sales.internal.web;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.sales.api.dto.SaleDto;
import com.nss.pibblest.modules.sales.internal.core.SaleService;
import com.nss.pibblest.modules.sales.internal.web.requests.cancelSale.CancelSaleResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleRequest;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.getSales.GetSalesResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sale")
public class SaleController {

    private final SaleService saleService;

    public SaleController (SaleService saleService){
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<CreateSaleResponse> createSale(@Valid @RequestBody CreateSaleRequest request){
        return saleService.createSale(request);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<GetSalesResponse> getSalesByStore(@PathVariable("storeId") Long storeId, Pageable pageable) {
        return saleService.getSalesByStore(storeId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleDto> getSaleById(@PathVariable("id") Long id) {
        return saleService.getSaleById(id);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<CancelSaleResponse> cancelSale(@PathVariable("id") Long id) {
        return saleService.cancelSale(id);
    }
}
