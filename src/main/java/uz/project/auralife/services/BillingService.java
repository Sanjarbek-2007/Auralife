package uz.project.auralife.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.project.auralife.domains.User;
import uz.project.auralife.domains.billing.Subscribtion;
import uz.project.auralife.domains.billing.Tarrifs;
import uz.project.auralife.domains.billing.Transaction;
import uz.project.auralife.repositories.SubscriptionRepository;
import uz.project.auralife.repositories.TransactionRepository;
import uz.project.auralife.repositories.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${click.service-id}")
    private String serviceId;

    @Value("${click.merchant-id}")
    private String merchantId;

    @Value("${click.secret-key}")
    private String secretKey;

    @Value("${click.return-url}")
    private String returnUrl;

    // ─── Plans ──────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAllPlans() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tarrifs tariff : Tarrifs.values()) {
            var plan = tariff.getPlan();
            Map<String, Object> planMap = new LinkedHashMap<>();
            planMap.put("key", tariff.name());
            planMap.put("id", plan.getId());
            planMap.put("name", plan.getName());
            planMap.put("description", plan.getDescription());
            planMap.put("price", plan.getPrice());
            planMap.put("currency", plan.getCurrency());
            planMap.put("durationInDays", plan.getDurationInDays());
            planMap.put("isActive", plan.getIsActive() == null || plan.getIsActive());

            List<Map<String, Object>> entitlements = new ArrayList<>();
            if (plan.getConditions() != null) {
                for (var e : plan.getConditions()) {
                    Map<String, Object> em = new LinkedHashMap<>();
                    em.put("featureKey", e.getFeatureKey());
                    em.put("displayName", e.getDisplayName());
                    em.put("description", e.getDescription());
                    em.put("amount", e.getAmount());
                    em.put("type", e.getType().name());
                    em.put("allowRollover", e.getAllowRollover());
                    entitlements.add(em);
                }
            }
            planMap.put("entitlements", entitlements);
            result.add(planMap);
        }
        return result;
    }

    // ─── Subscription ────────────────────────────────────────────────────────────

    public Optional<Subscribtion> getSubscription(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    // ─── Transactions ────────────────────────────────────────────────────────────

    public List<Transaction> getTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Transaction> getTransactionByMerchantId(String merchantTransId) {
        return transactionRepository.findByMerchantTransId(merchantTransId);
    }

    // ─── Create Payment Intent ────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> createPaymentIntent(Long userId, String tariffKey) {
        Tarrifs tariff = Tarrifs.valueOf(tariffKey);
        long amountTiyin = (long) (tariff.getPlan().getPrice() * 100); // tiyin

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create a pending transaction
        Transaction tx = Transaction.builder()
                .user(user)
                .tariff(tariff)
                .amount(amountTiyin)
                .currency(tariff.getPlan().getCurrency())
                .merchantTransId(UUID.randomUUID().toString())
                .status(Transaction.TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        tx = transactionRepository.save(tx);

        // Build Click payment URL
        String merchantTransId = tx.getMerchantTransId();
        double amountForClick = tariff.getPlan().getPrice(); // amount in UZS (not tiyin)

        String clickWebUrl = String.format(
                "https://my.click.uz/services/pay?service_id=%s&merchant_id=%s&amount=%.0f&transaction_param=%s&return_url=%s",
                serviceId, merchantId, amountForClick, merchantTransId, returnUrl
        );

        // Deep link for CLICK app (same URL scheme, opens in app if installed)
        String deepLink = String.format(
                "click://payment?service_id=%s&merchant_id=%s&amount=%.0f&transaction_param=%s",
                serviceId, merchantId, amountForClick, merchantTransId
        );

        // QR code data is the same web URL — user scans it in CLICK app
        String qrData = clickWebUrl;
        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" +
                java.net.URLEncoder.encode(qrData, StandardCharsets.UTF_8);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("merchantTransId", merchantTransId);
        response.put("transactionId", tx.getId());
        response.put("amount", amountForClick);
        response.put("currency", tariff.getPlan().getCurrency());
        response.put("clickWebUrl", clickWebUrl);
        response.put("deepLink", deepLink);
        response.put("qrImageUrl", qrImageUrl);
        return response;
    }

    // ─── Click SHOP-API Prepare ──────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> handlePrepare(Map<String, Object> req) {
        Map<String, Object> resp = new LinkedHashMap<>();

        try {
            String clickTransId = String.valueOf(req.get("click_trans_id"));
            String merchantTransId = String.valueOf(req.get("merchant_trans_id"));
            String amountStr = String.valueOf(req.get("amount"));
            String action = String.valueOf(req.get("action"));
            String signTime = String.valueOf(req.get("sign_time"));
            String receivedSign = String.valueOf(req.get("sign_string"));

            // Verify MD5 signature: MD5(click_trans_id + service_id + secret_key + merchant_trans_id + amount + action + sign_time)
            String signData = clickTransId + serviceId + secretKey + merchantTransId + amountStr + action + signTime;
            String computedMd5 = md5Hex(signData);

            if (!computedMd5.equalsIgnoreCase(receivedSign)) {
                log.warn("Click Prepare: invalid signature for merchantTransId={}", merchantTransId);
                resp.put("error", -1);
                resp.put("error_note", "Invalid sign");
                return resp;
            }

            Optional<Transaction> txOpt = transactionRepository.findByMerchantTransId(merchantTransId);
            if (txOpt.isEmpty()) {
                resp.put("error", -5);
                resp.put("error_note", "Order not found");
                return resp;
            }

            Transaction tx = txOpt.get();

            if (tx.getStatus() == Transaction.TransactionStatus.CANCELLED) {
                resp.put("error", -9);
                resp.put("error_note", "Transaction cancelled");
                return resp;
            }
            if (tx.getStatus() == Transaction.TransactionStatus.PAID) {
                resp.put("error", -4);
                resp.put("error_note", "Already paid");
                return resp;
            }

            // Store the Click transaction ID
            tx.setClickTransId(clickTransId);
            transactionRepository.save(tx);

            resp.put("click_trans_id", Long.parseLong(clickTransId));
            resp.put("merchant_trans_id", merchantTransId);
            resp.put("merchant_prepare_id", tx.getId());
            resp.put("error", 0);
            resp.put("error_note", "Success");
        } catch (Exception e) {
            log.error("Click Prepare error", e);
            resp.put("error", -8);
            resp.put("error_note", "Internal error: " + e.getMessage());
        }
        return resp;
    }

    // ─── Click SHOP-API Complete ─────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> handleComplete(Map<String, Object> req) {
        Map<String, Object> resp = new LinkedHashMap<>();

        try {
            String clickTransId = String.valueOf(req.get("click_trans_id"));
            String merchantTransId = String.valueOf(req.get("merchant_trans_id"));
            String merchantPrepareId = String.valueOf(req.get("merchant_prepare_id"));
            String amountStr = String.valueOf(req.get("amount"));
            String action = String.valueOf(req.get("action"));
            String signTime = String.valueOf(req.get("sign_time"));
            String receivedSign = String.valueOf(req.get("sign_string"));
            int error = Integer.parseInt(String.valueOf(req.get("error")));

            // Verify MD5 signature
            String signData = clickTransId + serviceId + secretKey + merchantTransId + merchantPrepareId + amountStr + action + signTime;
            String computedMd5 = md5Hex(signData);

            if (!computedMd5.equalsIgnoreCase(receivedSign)) {
                log.warn("Click Complete: invalid signature for merchantTransId={}", merchantTransId);
                resp.put("error", -1);
                resp.put("error_note", "Invalid sign");
                return resp;
            }

            Optional<Transaction> txOpt = transactionRepository.findByMerchantTransId(merchantTransId);
            if (txOpt.isEmpty()) {
                resp.put("error", -5);
                resp.put("error_note", "Order not found");
                return resp;
            }

            Transaction tx = txOpt.get();

            if (error < 0) {
                // Click cancelled the payment
                tx.setStatus(Transaction.TransactionStatus.CANCELLED);
                transactionRepository.save(tx);
                resp.put("error", -9);
                resp.put("error_note", "Transaction cancelled");
                return resp;
            }

            if (tx.getStatus() == Transaction.TransactionStatus.PAID) {
                resp.put("error", -4);
                resp.put("error_note", "Already paid");
                return resp;
            }

            // Mark transaction as PAID
            tx.setStatus(Transaction.TransactionStatus.PAID);
            tx.setPaidAt(LocalDateTime.now());
            transactionRepository.save(tx);

            // Activate or extend subscription
            activateSubscription(tx);

            resp.put("click_trans_id", Long.parseLong(clickTransId));
            resp.put("merchant_trans_id", merchantTransId);
            resp.put("merchant_confirm_id", tx.getId());
            resp.put("error", 0);
            resp.put("error_note", "Success");
        } catch (Exception e) {
            log.error("Click Complete error", e);
            resp.put("error", -8);
            resp.put("error_note", "Internal error: " + e.getMessage());
        }
        return resp;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private void activateSubscription(Transaction tx) {
        Long userId = tx.getUser().getId();
        Subscribtion sub = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscribtion.builder()
                        .user(tx.getUser())
                        .build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = (sub.getIsActive() != null && sub.getIsActive() && sub.getEndDate() != null && sub.getEndDate().isAfter(now))
                ? sub.getEndDate() // extend from current end if still active
                : now;

        sub.setTarrif(tx.getTariff());
        sub.setStartDate(now);
        sub.setEndDate(base.plusDays(tx.getTariff().getPlan().getDurationInDays()));
        sub.setNextPaymentDate(base.plusDays(tx.getTariff().getPlan().getDurationInDays()));
        sub.setIsActive(true);
        subscriptionRepository.save(sub);
    }

    private String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 error", e);
        }
    }
}
