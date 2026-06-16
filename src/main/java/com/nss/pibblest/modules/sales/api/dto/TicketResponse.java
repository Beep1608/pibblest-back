package com.nss.pibblest.modules.sales.api.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class TicketResponse {
    private String saleId;
    private String timestamp;
    private List<TicketLineItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal total;
}