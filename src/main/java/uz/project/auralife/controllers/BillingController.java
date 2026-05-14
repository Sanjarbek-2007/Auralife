package uz.project.auralife.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.domains.billing.Subscribtion;
import uz.project.auralife.domains.billing.Transaction;
import uz.project.auralife.services.BillingService;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final UserContext userContext;

    // ─── 1. List all tariff plans (publicly accessible) ─────────────────────────

    @GetMapping("/plans")
    public ResponseEntity<List<Map<String, Object>>> getPlans() {
        return ResponseEntity.ok(billingService.getAllPlans());
    }

    // ─── 2. Current user's subscription ─────────────────────────────────────────

    @GetMapping("/my-subscription")
    public ResponseEntity<?> getMySubscription() {
        Long userId = userContext.getUserId();
        Optional<Subscribtion> sub = billingService.getSubscription(userId);
        if (sub.isEmpty()) {
            return ResponseEntity.ok(Map.of("active", false));
        }
        Subscribtion s = sub.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("active", Boolean.TRUE.equals(s.getIsActive()));
        resp.put("tariff", s.getTarrif() != null ? s.getTarrif().name() : null);
        resp.put("planName", s.getTarrif() != null ? s.getTarrif().getPlan().getName() : null);
        resp.put("price", s.getTarrif() != null ? s.getTarrif().getPlan().getPrice() : null);
        resp.put("currency", s.getTarrif() != null ? s.getTarrif().getPlan().getCurrency() : null);
        resp.put("startDate", s.getStartDate() != null ? s.getStartDate().format(fmt) : null);
        resp.put("endDate", s.getEndDate() != null ? s.getEndDate().format(fmt) : null);
        resp.put("nextPaymentDate", s.getNextPaymentDate() != null ? s.getNextPaymentDate().format(fmt) : null);
        return ResponseEntity.ok(resp);
    }

    // ─── 3. Current user's transactions ─────────────────────────────────────────

    @GetMapping("/my-transactions")
    public ResponseEntity<List<Map<String, Object>>> getMyTransactions() {
        Long userId = userContext.getUserId();
        List<Transaction> txs = billingService.getTransactions(userId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        List<Map<String, Object>> result = txs.stream().map(tx -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", tx.getId());
            m.put("tariff", tx.getTariff().name());
            m.put("planName", tx.getTariff().getPlan().getName());
            m.put("amount", tx.getTariff().getPlan().getPrice());
            m.put("currency", tx.getCurrency());
            m.put("status", tx.getStatus().name());
            m.put("createdAt", tx.getCreatedAt() != null ? tx.getCreatedAt().format(fmt) : null);
            m.put("paidAt", tx.getPaidAt() != null ? tx.getPaidAt().format(fmt) : null);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─── 4. Create payment intent (returns Click payment URLs) ──────────────────

    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, String> body) {
        Long userId = userContext.getUserId();
        String tariffKey = body.get("tariff");
        if (tariffKey == null || tariffKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "tariff is required"));
        }
        try {
            Map<String, Object> result = billingService.createPaymentIntent(userId, tariffKey);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown tariff: " + tariffKey));
        }
    }

    // ─── 5. Poll transaction status (frontend calls this after Click redirect) ──

    @GetMapping("/transaction-status/{merchantTransId}")
    public ResponseEntity<Map<String, Object>> getTransactionStatus(@PathVariable String merchantTransId) {
        return billingService.getTransactionByMerchantId(merchantTransId)
                .map(tx -> {
                    Map<String, Object> resp = new LinkedHashMap<>();
                    resp.put("merchantTransId", merchantTransId);
                    resp.put("status", tx.getStatus().name());
                    resp.put("paidAt", tx.getPaidAt() != null ? tx.getPaidAt().toString() : null);
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── 5. Click SHOP-API — Prepare webhook (no auth, Click calls this) ────────

    @PostMapping("/click/prepare")
    public ResponseEntity<Map<String, Object>> clickPrepare(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(billingService.handlePrepare(req));
    }

    // ─── 6. Click SHOP-API — Complete webhook (no auth, Click calls this) ───────

    @PostMapping("/click/complete")
    public ResponseEntity<Map<String, Object>> clickComplete(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(billingService.handleComplete(req));
    }
}
