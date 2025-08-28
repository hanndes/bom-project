package com.handederelii.bom_project.dto.response;

public record BomResponse(
        String mpn,
        int quantity,
        String supplier,
        double unitPrice,
        double totalPrice,
        String requestId
) {}