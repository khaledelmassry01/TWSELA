package com.twsela.service;

import com.twsela.domain.Invoice;
import com.twsela.domain.Invoice.InvoiceStatus;
import com.twsela.domain.InvoiceItem;
import com.twsela.domain.MerchantSubscription;
import com.twsela.repository.InvoiceRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Manages invoice generation, payment, and lifecycle.
 */
@Service
@Transactional
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.14"); // 14% VAT
    private static final String INVOICE_PREFIX = "TWS-INV-";

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionService subscriptionService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          SubscriptionService subscriptionService) {
        this.invoiceRepository = invoiceRepository;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Generate an invoice for a merchant subscription period.
     */
    public Invoice generateInvoice(Long subscriptionId) {
        MerchantSubscription sub = subscriptionService.getActiveSubscription(
                subscriptionId); // We'll pass merchantId

        return generateInvoiceForSubscription(sub);
    }

    /**
     * Generate an invoice for a given subscription.
     */
    public Invoice generateInvoiceForSubscription(MerchantSubscription subscription) {
        // Check for duplicate invoice in current period
        boolean exists = invoiceRepository.existsBySubscriptionIdAndStatus(
                subscription.getId(), InvoiceStatus.PENDING);
        if (exists) {
            throw new BusinessRuleException("يوجد فاتورة معلقة بالفعل لهذا الاشتراك");
        }

        BigDecimal amount = calculateSubscriptionAmount(subscription);
        BigDecimal tax = amount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(tax);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setSubscription(subscription);
        invoice.setAmount(amount);
        invoice.setTax(tax);
        invoice.setTotalAmount(total);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setDueDate(Instant.now().plus(7, ChronoUnit.DAYS));

        // Add line item
        InvoiceItem item = new InvoiceItem(
                "اشتراك " + subscription.getPlan().getName().name() + " - "
                        + subscription.getBillingCycle().name(),
                1,
                amount
        );
        item.setInvoice(invoice);
        invoice.getItems().add(item);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} generated for subscription {} (total: {})",
                saved.getInvoiceNumber(), subscription.getId(), total);
        return saved;
    }

    /**
     * Get an invoice by its ID.
     */
    @Transactional(readOnly = true)
    public Invoice getInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
    }

    /**
     * Get an invoice by number.
     */
    @Transactional(readOnly = true)
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
    }

    /**
     * Get paginated invoices for a merchant.
     */
    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByMerchant(Long merchantId, Pageable pageable) {
        return invoiceRepository.findBySubscriptionMerchantIdOrderByCreatedAtDesc(merchantId, pageable);
    }

    /**
     * Get invoices by status (admin).
     */
    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    /**
     * Mark an invoice as paid.
     */
    public Invoice markAsPaid(Long invoiceId, String paymentGateway) {
        Invoice invoice = getInvoice(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.PENDING && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            throw new BusinessRuleException("لا يمكن دفع فاتورة بحالة: " + invoice.getStatus());
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        invoice.setPaymentGateway(paymentGateway);
        invoice.setUpdatedAt(Instant.now());

        // Activate/renew the subscription
        subscriptionService.activate(invoice.getSubscription().getId());

        log.info("Invoice {} paid via {}", invoice.getInvoiceNumber(), paymentGateway);
        return invoiceRepository.save(invoice);
    }

    /**
     * Refund an invoice.
     */
    public Invoice refundInvoice(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new BusinessRuleException("لا يمكن استرداد فاتورة غير مدفوعة");
        }

        invoice.setStatus(InvoiceStatus.REFUNDED);
        invoice.setUpdatedAt(Instant.now());

        log.info("Invoice {} refunded", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    /**
     * Cancel an invoice.
     */
    public Invoice cancelInvoice(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.REFUNDED) {
            throw new BusinessRuleException("لا يمكن إلغاء فاتورة مدفوعة أو مستردة");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setUpdatedAt(Instant.now());

        log.info("Invoice {} cancelled", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    /**
     * Process overdue invoices — runs daily at 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void processOverdueInvoices() {
        Instant now = Instant.now();
        List<Invoice> overdue = invoiceRepository.findOverdue(now);
        for (Invoice invoice : overdue) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoice.setUpdatedAt(now);
            invoiceRepository.save(invoice);
            log.warn("Invoice {} is now OVERDUE (due: {})", invoice.getInvoiceNumber(), invoice.getDueDate());
        }
    }

    private BigDecimal calculateSubscriptionAmount(MerchantSubscription subscription) {
        return switch (subscription.getBillingCycle()) {
            case MONTHLY -> subscription.getPlan().getMonthlyPrice();
            case ANNUAL -> subscription.getPlan().getAnnualPrice();
        };
    }

    private String generateInvoiceNumber() {
        return INVOICE_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
