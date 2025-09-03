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

        return idempotencyService.runOnce(
                "bom.quote",                 // 1) scope
                req,                         // 2) body
                Duration.ofSeconds(120),     // 3) ttl
                () -> {                      // 4) Supplier<BomResponse>
                    var quote = bomQuoteService.computeQuote(req.mpn(), req.quantity());
                    String requestId = redisBomService.save(
                            req.mpn(), req.quantity(), username, Duration.ofDays(1)
                    );
                    return new BomResponse(
                            quote.mpn(), quote.quantity(), quote.supplier(),
                            quote.unitPrice().doubleValue(), quote.totalPrice().doubleValue(),
                            requestId
                    );
                },
                username ,BomResponse.class                    // 5) actorKey
        );
    }

    @GetMapping("/{requestId}")
    public String getRaw(@PathVariable String requestId) {
        return redisBomService.getJson(requestId).orElse("Not found");
    }
}
