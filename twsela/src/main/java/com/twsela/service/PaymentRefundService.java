package com.twsela.service;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentRefund;
import com.twsela.domain.User;
import com.twsela.repository.PaymentIntentRepository;
import com.twsela.repository.PaymentRefundRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * خدمة الاستردادات — إنشاء وموافقة ورفض ومعالجة طلبات الاسترداد.
 */
@Service
@Transactional
public class PaymentRefundService {

    private static final Logger log = LoggerFactory.getLogger(PaymentRefundService.class);

    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;

    public PaymentRefundService(PaymentRefundRepository paymentRefundRepository,
                                PaymentIntentRepository paymentIntentRepository,
                                UserRepository userRepository,
                                PaymentGatewayFactory paymentGatewayFactory) {
        this.paymentRefundRepository = paymentRefundRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.userRepository = userRepository;
        this.paymentGatewayFactory = paymentGatewayFactory;
    }

    /**
     * إنشاء طلب استرداد.
     */
    public PaymentRefund createRefund(Long paymentIntentId, BigDecimal amount, String reason) {
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentIntent", "id", paymentIntentId));

        if (intent.getStatus() != PaymentIntent.IntentStatus.SUCCEEDED) {
            throw new BusinessRuleException("لا يمكن استرداد دفعة غير ناجحة — الحالة: " + intent.getStatus());
        }

        // Check total refunded amount doesn't exceed original
        BigDecimal alreadyRefunded = paymentRefundRepository.findByPaymentIntentId(paymentIntentId).stream()
                .filter(r -> r.getStatus() != PaymentRefund.RefundStatus.REJECTED)
                .map(PaymentRefund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (alreadyRefunded.add(amount).compareTo(intent.getAmount()) > 0) {
            throw new BusinessRuleException("مبلغ الاسترداد يتجاوز المبلغ الأصلي");
        }

        PaymentRefund refund = new PaymentRefund();
        refund.setPaymentIntent(intent);
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus(PaymentRefund.RefundStatus.PENDING);

        PaymentRefund saved = paymentRefundRepository.save(refund);
        log.info("Refund request {} created for payment intent {} — amount={}", saved.getId(), paymentIntentId, amount);
        return saved;
    }

    /**
     * الموافقة على طلب استرداد ومعالجته عبر البوابة.
     */
    public PaymentRefund approveRefund(Long refundId, Long approvedByUserId) {
        PaymentRefund refund = getById(refundId);

        if (refund.getStatus() != PaymentRefund.RefundStatus.PENDING) {
            throw new BusinessRuleException("لا يمكن الموافقة على استرداد بحالة: " + refund.getStatus());
        }

        User approver = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approvedByUserId));

        refund.setApprovedBy(approver);
        refund.setApprovedAt(Instant.now());
        refund.setStatus(PaymentRefund.RefundStatus.APPROVED);

        // Process refund through gateway
        try {
            PaymentIntent intent = refund.getPaymentIntent();
            PaymentGateway gateway = paymentGatewayFactory.getGateway(intent.getProvider());
            String providerRef = gateway.refund(intent.getProviderRef(), refund.getAmount());

            refund.setProviderRef(providerRef);
            refund.setStatus(PaymentRefund.RefundStatus.PROCESSED);
            refund.setProcessedAt(Instant.now());

            // Update intent status
            intent.setStatus(PaymentIntent.IntentStatus.REFUNDED);
            intent.setUpdatedAt(Instant.now());
            paymentIntentRepository.save(intent);

            log.info("Refund {} approved and processed — providerRef={}", refundId, providerRef);
        } catch (Exception e) {
            log.error("Refund {} gateway processing failed: {}", refundId, e.getMessage());
            refund.setStatus(PaymentRefund.RefundStatus.APPROVED); // stays approved, retry later
        }

        return paymentRefundRepository.save(refund);
    }

    /**
     * رفض طلب استرداد.
     */
    public PaymentRefund rejectRefund(Long refundId, String rejectedReason) {
        PaymentRefund refund = getById(refundId);

        if (refund.getStatus() != PaymentRefund.RefundStatus.PENDING) {
            throw new BusinessRuleException("لا يمكن رفض استرداد بحالة: " + refund.getStatus());
        }

        refund.setStatus(PaymentRefund.RefundStatus.REJECTED);
        refund.setRejectedReason(rejectedReason);
        log.info("Refund {} rejected — reason: {}", refundId, rejectedReason);
        return paymentRefundRepository.save(refund);
    }

    @Transactional(readOnly = true)
    public PaymentRefund getById(Long refundId) {
        return paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentRefund", "id", refundId));
    }

    @Transactional(readOnly = true)
    public List<PaymentRefund> getByStatus(PaymentRefund.RefundStatus status) {
        return paymentRefundRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<PaymentRefund> getPendingRefunds() {
        return paymentRefundRepository.findByStatusOrderByCreatedAtDesc(PaymentRefund.RefundStatus.PENDING);
    }
}
