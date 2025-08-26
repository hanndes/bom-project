package com.handederelii.bom_project.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomResponse {
    private String mpn;
    private Integer quantity;

    // aşağıdakiler örnek alanlar; ihtiyacına göre azalt/artır
    private Boolean available;          // stok var mı
    private Integer leadTimeDays;       // tedarik süresi
    private BigDecimal unitPrice;       // birim fiyat
    private String currency;            // "USD", "TRY" vb.
    private BigDecimal totalPrice;      // unitPrice * quantity
    private String supplier;              // tedarikçi/kanal adı
}