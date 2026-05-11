package com.nss.pibblest.modules.sales.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.sales.internal.core.SaleService;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleRequest;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleResponse;

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
    
}
