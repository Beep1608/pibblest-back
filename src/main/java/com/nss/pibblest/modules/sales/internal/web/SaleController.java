package com.nss.pibblest.modules.sales.internal.web;

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
    @PreAuthorize("hasRole('OWNER') or @authZ.check(#request.storeId, 'MODULE_SALES', 'CREATE')")
    public ResponseEntity<CreateSaleResponse> createSale(@Valid @RequestBody CreateSaleRequest request){
        return saleService.createSale(request);
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Obtener ventas de una tienda", 
        description = "Permite listar las ventas. Un empleado común solo verá las suyas ('PERSONAL'). Si tiene permisos de lectura, podrá enviar '?scope=ALL' para ver todo el historial de la tienda."
    )
    public ResponseEntity<GetSalesResponse> getSalesByStore(
            @PathVariable("storeId") Long storeId, 
            @org.springframework.web.bind.annotation.RequestParam(value = "scope", defaultValue = "PERSONAL") String scope,
            Pageable pageable) {
        return saleService.getSalesByStore(storeId, scope, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<SaleDto> getSaleById(@PathVariable("id") Long id) {
        return saleService.getSaleById(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<CancelSaleResponse> cancelSale(@PathVariable("id") Long id) {
        return saleService.cancelSale(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteSale(@PathVariable("id") Long id) {
        return saleService.deleteSale(id);
    }
}
