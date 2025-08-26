package com.handederelii.bom_project.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomRequest {

    @NotBlank(message = "mpn boş olamaz")
    @Size(max = 100, message = "mpn en fazla 100 karakter olabilir")
    // İstersen daha katı bir pattern de koyabilirsin:
    // @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "mpn sadece harf, rakam ve . _ - içerebilir")
    private String mpn;

    @NotNull(message = "quantity zorunludur")
    @Min(value = 1, message = "quantity en az 1 olmalı")
    @Max(value = 1_000_000, message = "quantity çok büyük")
    private Integer quantity;
}