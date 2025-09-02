package com.handederelii.bom_project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BomRequest(
        @NotBlank String mpn,
        @Min(1) int quantity
) {}