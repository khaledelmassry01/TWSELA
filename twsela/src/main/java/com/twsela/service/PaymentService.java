package com.twsela.service;

import com.twsela.domain.PaymentTransaction;
import com.twsela.domain.PaymentTransaction.*;
import com.twsela.repository.PaymentTransactionRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates payment transactions through payment gateways.
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final InvoiceService invoiceService;

    public PaymentService(PaymentTransactionRepository transactionRepository,
                          PaymentGatewayFactory gatewayFactory,
                          InvoiceService invoiceService) {
        this.transactionRepository = transactionRepository;
        this.gatewayFactory = gatewayFactory;
        this.invoiceService = invoiceService;
    }

    /**
     * Initiate a payment for an invoice.
     */
    public PaymentTransaction initiatePayment(Long invoiceId, Long merchantId,
                                               PaymentGatewayType gatewayType,
                                               BigDecimal amount, String currency) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setGateway(gatewayType);
        tx.setType(PaymentType.CHARGE);
        tx.setStatus(PaymentStatus.PENDING);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setMerchantId(merchantId);
        tx.setInvoiceId(invoiceId);

        tx = transactionRepository.save(tx);

        try {
            PaymentGateway gateway = gatewayFactory.getGateway(gatewayType);
            String externalId = gateway.charge(amount, currency,
                    Map.of("invoiceId", invoiceId.toString(),
                            "merchantId", merchantId.toString(),
                            "txId", tx.getId().toString()));
            tx.setExternalId(externalId);
            tx.setStatus(PaymentStatus.SUCCESS);
            tx.setUpdatedAt(Instant.now());

            // Mark the invoice as paid
            invoiceService.markAsPaid(invoiceId, gatewayType.name());

            log.info("Payment {} successful for invoice {} via {}",
                    tx.getId(), invoiceId, gatewayType);
        } catch (Exception e) {
            tx.setStatus(PaymentStatus.FAILED);
            tx.setErrorMessage(e.getMessage());
            tx.setUpdatedAt(Instant.now());
            log.error("Payment {} failed for invoice {}: {}", tx.getId(), invoiceId, e.getMessage());
        }

        return transactionRepository.save(tx);
    }

    /**
     * Process a refund for a transaction.
     */
    public PaymentTransaction refund(Long transactionId) {
        PaymentTransaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "id", transactionId));

        if (original.getStatus() != PaymentStatus.SUCCESS) {
            throw new com.twsela.web.exception.BusinessRuleException(
                    "لا يمكن استرداد معاملة غير ناجحة");
        }

        PaymentGateway gateway = gatewayFactory.getGateway(original.getGateway());
        String refundExternalId = gateway.refund(original.getExternalId(), original.getAmount());

        PaymentTransaction refundTx = new PaymentTransaction();
        refundTx.setGateway(original.getGateway());
        refundTx.setType(PaymentType.REFUND);
        refundTx.setStatus(PaymentStatus.SUCCESS);
        refundTx.setExternalId(refundExternalId);
        refundTx.setAmount(original.getAmount());
        refundTx.setCurrency(original.getCurrency());
        refundTx.setMerchantId(original.getMerchantId());
        refundTx.setInvoiceId(original.getInvoiceId());

        original.setStatus(PaymentStatus.REFUNDED);
        original.setUpdatedAt(Instant.now());
        transactionRepository.save(original);

        // Refund the invoice too
        if (original.getInvoiceId() != null) {
            invoiceService.refundInvoice(original.getInvoiceId());
        }

        log.info("Refund {} processed for original transaction {}", refundExternalId, transactionId);
        return transactionRepository.save(refundTx);
    }

    /**
     * Get transactions for a merchant.
     */
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getTransactionsByMerchant(Long merchantId) {
        return transactionRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    /**
     * Get transactions for an invoice.
     */
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getTransactionsByInvoice(Long invoiceId) {
        return transactionRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Find a transaction by its external gateway ID.
     */
    @Transactional(readOnly = true)
    public PaymentTransaction getByExternalId(String externalId) {
        return transactionRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", "externalId", externalId));
    }
}
