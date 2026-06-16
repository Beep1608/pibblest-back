package com.nss.pibblest.modules.sales.internal.web;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.ZonedDateTime;

import com.nss.pibblest.modules.sales.api.dto.SaleDto;
import com.nss.pibblest.modules.sales.internal.core.SaleService;
import com.nss.pibblest.modules.sales.internal.web.requests.cancelSale.CancelSaleResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleRequest;
import com.nss.pibblest.modules.sales.internal.web.requests.createSale.CreateSaleResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.getSales.GetSalesResponse;
import com.nss.pibblest.modules.sales.internal.web.requests.ticket.TicketRequest;
import com.nss.pibblest.modules.sales.api.dto.TicketResponse;

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

    @PostMapping("/ticket")
    @PreAuthorize("hasRole('OWNER') or @authZ.check(#request.storeId, 'MODULE_SALES', 'CREATE')")
    public ResponseEntity<TicketResponse> getTicket(@Valid @RequestBody TicketRequest request){
        return saleService.getTicket(request);
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('EMPLOYEE')")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Obtener ventas de una tienda", 
        description = "Permite listar las ventas. Si se usa scope=ALL y el usuario tiene permisos de lectura, puede enviar opcionalmente un employeeId para filtrar las ventas de un usuario específico."
    )
    public ResponseEntity<GetSalesResponse> getSalesByStore(
            @PathVariable("storeId") Long storeId, 
            @RequestParam(value = "scope", defaultValue = "PERSONAL") String scope,
            @RequestParam(value = "employeeId", required = false) java.util.UUID targetEmployeeId,
            // ✨ FIX Hallazgo #1: Inyección de parámetros de rangos de fecha opcionales
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            Pageable pageable) {
        return saleService.getSalesByStore(storeId, scope, targetEmployeeId, startDate, endDate, pageable);
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
