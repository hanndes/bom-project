package com.handederelii.bom_project.domain;

import java.math.BigDecimal;

public record BomQuote(
        String mpn,
        int quantity,
        String supplier,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}