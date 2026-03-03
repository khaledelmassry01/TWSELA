package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentMethod;
import com.twsela.domain.PaymentTransaction;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.domain.Shipment;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.PaymentMethodRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * خدمة نوايا الدفع — إنشاء وإدارة وتأكيد وإلغاء نوايا الدفع.
 */
@Service
@Transactional
public class PaymentIntentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentIntentService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final int EXPIRY_MINUTES = 30;

    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;

    public PaymentIntentService(PaymentIntentRepository paymentIntentRepository,
                                PaymentMethodRepository paymentMethodRepository,
                                ShipmentRepository shipmentRepository,
                                PaymentGatewayFactory paymentGatewayFactory) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.shipmentRepository = shipmentRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
    }

    /**
     * إنشاء نية دفع جديدة لشحنة.
     */
    public PaymentIntent createIntent(Long shipmentId, BigDecimal amount, String currency,
                                       PaymentGatewayType gatewayType, Long paymentMethodId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        PaymentIntent intent = new PaymentIntent();
        intent.setShipment(shipment);
        intent.setAmount(amount);
        intent.setCurrency(currency != null ? currency : "EGP");
        intent.setStatus(PaymentIntent.IntentStatus.PENDING);
        intent.setProvider(gatewayType);
        intent.setAttempts(0);
        intent.setExpiresAt(Instant.now().plus(EXPIRY_MINUTES, ChronoUnit.MINUTES));

        if (paymentMethodId != null) {
            PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                    .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod", "id", paymentMethodId));
            intent.setPaymentMethod(method);
        }

        PaymentIntent saved = paymentIntentRepository.save(intent);
        log.info("Payment intent {} created for shipment {} — amount={} {}, gateway={}",
                saved.getId(), shipmentId, amount, currency, gatewayType);
        return saved;
    }

    /**
     * تأكيد نية الدفع — يتم إرسالها للبوابة للمعالجة.
     */
    public PaymentIntent confirmIntent(Long intentId) {
        PaymentIntent intent = getById(intentId);

        if (intent.getStatus() != PaymentIntent.IntentStatus.PENDING) {
            throw new BusinessRuleException("لا يمكن تأكيد نية دفع بحالة: " + intent.getStatus());
        }

        if (intent.getExpiresAt() != null && intent.getExpiresAt().isBefore(Instant.now())) {
            intent.setStatus(PaymentIntent.IntentStatus.FAILED);
            intent.setFailureReason("انتهت صلاحية نية الدفع");
            intent.setUpdatedAt(Instant.now());
            paymentIntentRepository.save(intent);
            throw new BusinessRuleException("انتهت صلاحية نية الدفع");
        }

        intent.setStatus(PaymentIntent.IntentStatus.PROCESSING);
        intent.setAttempts(intent.getAttempts() + 1);
        intent.setUpdatedAt(Instant.now());

        try {
            PaymentGateway gateway = paymentGatewayFactory.getGateway(intent.getProvider());
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("intentId", String.valueOf(intent.getId()));
            metadata.put("shipmentId", String.valueOf(intent.getShipment().getId()));

            String providerRef = gateway.charge(intent.getAmount(), intent.getCurrency(), metadata);
            intent.setProviderRef(providerRef);
            intent.setStatus(PaymentIntent.IntentStatus.SUCCEEDED);
            intent.setConfirmedAt(Instant.now());
            log.info("Payment intent {} confirmed — providerRef={}", intentId, providerRef);
        } catch (Exception e) {
            log.error("Payment intent {} failed on attempt {}: {}", intentId, intent.getAttempts(), e.getMessage());
            if (intent.getAttempts() >= MAX_ATTEMPTS) {
                intent.setStatus(PaymentIntent.IntentStatus.FAILED);
                intent.setFailureReason("فشل بعد " + MAX_ATTEMPTS + " محاولات: " + e.getMessage());
                intent.setFailedAt(Instant.now());
            } else {
                intent.setStatus(PaymentIntent.IntentStatus.PENDING);
            }
        }

        intent.setUpdatedAt(Instant.now());
        return paymentIntentRepository.save(intent);
    }

    /**
     * إلغاء نية دفع.
     */
    public PaymentIntent cancelIntent(Long intentId) {
        PaymentIntent intent = getById(intentId);

        if (intent.getStatus() != PaymentIntent.IntentStatus.PENDING) {
            throw new BusinessRuleException("لا يمكن إلغاء نية دفع بحالة: " + intent.getStatus());
        }

        intent.setStatus(PaymentIntent.IntentStatus.CANCELLED);
        intent.setUpdatedAt(Instant.now());
        log.info("Payment intent {} cancelled", intentId);
        return paymentIntentRepository.save(intent);
    }

    /**
     * الحصول على نية دفع بالمعرف.
     */
    @Transactional(readOnly = true)
    public PaymentIntent getById(Long intentId) {
        return paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentIntent", "id", intentId));
    }

    /**
     * الحصول على نوايا الدفع لشحنة.
     */
    @Transactional(readOnly = true)
    public List<PaymentIntent> getByShipmentId(Long shipmentId) {
        return paymentIntentRepository.findByShipmentId(shipmentId);
    }

    /**
     * تنظيف نوايا الدفع المنتهية.
     */
    public int expireOldIntents() {
        List<PaymentIntent> expired = paymentIntentRepository.findExpired(Instant.now());
        int count = 0;
        for (PaymentIntent intent : expired) {
            intent.setStatus(PaymentIntent.IntentStatus.FAILED);
            intent.setFailureReason("انتهت صلاحية نية الدفع تلقائياً");
            intent.setFailedAt(Instant.now());
            intent.setUpdatedAt(Instant.now());
            paymentIntentRepository.save(intent);
            count++;
        }
        log.info("Expired {} stale payment intents", count);
        return count;
    }
}
