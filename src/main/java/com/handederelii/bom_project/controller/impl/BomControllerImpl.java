package com.handederelii.bom_project.controller.impl;

import com.handederelii.bom_project.controller.IBomController;
import com.handederelii.bom_project.dto.request.BomRequest;
import com.handederelii.bom_project.dto.response.BomResponse;
import com.handederelii.bom_project.service.BomQuoteService;
import com.handederelii.bom_project.service.IdempotencyService;
import com.handederelii.bom_project.service.RedisBomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;

@RestController
@RequestMapping("/bom")
@RequiredArgsConstructor
public class BomControllerImpl implements IBomController {

    private final RedisBomService redisBomService;   // log
    private final BomQuoteService bomQuoteService;   // iş kuralı
    private final IdempotencyService idempotencyService; // ⬅ eklendi

    @PostMapping("/query")
    public BomResponse query(@Valid @RequestBody BomRequest req, Principal principal) {
        if (req.quantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        String username = principal != null ? principal.getName() : "anonymous";

        // Idempotency: Aynı scope + body (+user) için sadece 1 kez çalıştır
        return idempotencyService.runOnce(
                "POST:/bom/query",   // scope: endpoint'i açıkça yaz
                req,                 // body: hash bunun üzerinden üretilecek
                Duration.ofSeconds(120), // TTL: istersen değiştir (örn. ofMinutes(10))
                () -> {              // action: yaptığın tüm işi buraya koy
                    // 1) İş kuralı (quote)
                    var quote = bomQuoteService.computeQuote(req.mpn(), req.quantity());

                    // 2) Redis’e 1 gün TTL ile log
                    String requestId = redisBomService.save(
                            req.mpn(), req.quantity(), username, Duration.ofDays(1));

                    // 3) HTTP cevabı
                    return new BomResponse(
                            quote.mpn(), quote.quantity(), quote.supplier(),
                            quote.unitPrice().doubleValue(), quote.totalPrice().doubleValue(),
                            requestId);
                },
                username              // actorId: kullanıcıya özel idempotency
        );
    }


    @GetMapping("/{requestId}")
    public String getRaw(@PathVariable String requestId) {
        return redisBomService.getJson(requestId).orElse("Not found");
    }
}
