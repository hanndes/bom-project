package com.handederelii.bom_project.service;

import com.handederelii.bom_project.domain.BomQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BomQuoteService {

    private final IdempotencyService idempotencyService;

    public BomQuote computeQuote(String mpn, int qty) {
        // Hashlenecek body benzeri bir DTO üretiyoruz
        record Req(String mpn, int qty) {}
        Req body = new Req(mpn, qty);

        return idempotencyService.runOnce(
                "POST:/bom/query",        // scope
                body,                      // hash için kullanılacak body
                Duration.ofMillis(120),       // TTL
                () -> doComputeQuote(mpn, qty),  // iş mantığı
                null                       // kullanıcıya özgü yapmak istersen auth.getName() gönder
        );
    }

    private BomQuote doComputeQuote(String mpn, int qty) {
        String supplier = pickSupplier(mpn);
        BigDecimal unit = calculateUnitPrice(mpn, qty);
        BigDecimal total = unit.multiply(BigDecimal.valueOf(qty));
        return new BomQuote(mpn, qty, supplier, unit, total);
    }

    private String pickSupplier(String mpn) {
        String[] suppliers = {"ACME", "Mouser", "DigiKey", "Arrow", "Avnet"};
        int h = (mpn == null ? 0 : mpn.toUpperCase(Locale.ROOT).hashCode());
        return suppliers[Math.abs(h) % suppliers.length];
    }

    private BigDecimal calculateUnitPrice(String mpn, int qty) {
        BigDecimal base = basePrice(mpn);
        BigDecimal discount = discountForQty(qty);
        BigDecimal price = base.multiply(BigDecimal.ONE.subtract(discount));
        if (qty < 10) price = price.add(new BigDecimal("0.30"));
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal basePrice(String mpn) {
        if (mpn == null)
            return new BigDecimal("11.70");
        String u = mpn.toUpperCase(Locale.ROOT);
        if (u.startsWith("TPS"))
            return new BigDecimal("13.20");
        if (u.startsWith("STM"))
            return new BigDecimal("10.10");
        if (u.startsWith("LM"))
            return new BigDecimal("9.80");
        if (u.startsWith("AD"))
            return new BigDecimal("14.90");
        return new BigDecimal("11.70");
    }

    private BigDecimal discountForQty(int q) {
        if (q >= 1000) return new BigDecimal("0.15");
        if (q >= 500) return new BigDecimal("0.10");
        if (q >= 100) return new BigDecimal("0.07");
        if (q >= 50) return new BigDecimal("0.03");
        return BigDecimal.ZERO;
    }
}
