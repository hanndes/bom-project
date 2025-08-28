package com.handederelii.bom_project.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;


public record BomRequest(
        @NotBlank String mpn,
        @Min(1) int quantity
) {}