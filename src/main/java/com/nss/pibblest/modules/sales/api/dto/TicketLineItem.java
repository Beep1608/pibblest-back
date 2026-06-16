package com.nss.pibblest.modules.sales.api.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class TicketLineItem {
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}