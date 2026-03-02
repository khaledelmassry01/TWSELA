package com.twsela.service;

import com.twsela.domain.EInvoice;
import com.twsela.domain.Invoice;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import com.twsela.repository.EInvoiceRepository;
import com.twsela.repository.InvoiceRepository;
import com.twsela.web.dto.CountryDTO.EInvoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(EInvoiceService.class);

    private final EInvoiceRepository eInvoiceRepository;
    private final InvoiceRepository invoiceRepository;

    public EInvoiceService(EInvoiceRepository eInvoiceRepository,
                           InvoiceRepository invoiceRepository) {
        this.eInvoiceRepository = eInvoiceRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Generate an e-invoice from an existing invoice.
     */
    public EInvoiceResponse generateEInvoice(Long invoiceId, String countryCode) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة غير موجودة: " + invoiceId));

        if (eInvoiceRepository.findByInvoiceId(invoiceId).isPresent()) {
            throw new BusinessRuleException("الفاتورة الإلكترونية موجودة بالفعل لهذه الفاتورة");
        }

        EInvoice.EInvoiceFormat format = resolveFormat(countryCode);

        EInvoice ei = new EInvoice();
        ei.setInvoice(invoice);
        ei.setCountryCode(countryCode.toUpperCase());
        ei.setFormat(format);
        ei.setSerialNumber(generateSerialNumber(countryCode));
        ei.setStatus(EInvoice.EInvoiceStatus.DRAFT);

        // Sign the payload
        String payload = buildPayload(invoice, countryCode);
        ei.setSignedPayload(signPayload(payload));
        ei.setQrCode(generateQrCode(ei));

        ei = eInvoiceRepository.save(ei);
        log.info("Generated e-invoice {} for invoice {}", ei.getSerialNumber(), invoiceId);
        return toResponse(ei);
    }

    @Transactional(readOnly = true)
    public EInvoiceResponse getById(Long id) {
        EInvoice ei = eInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة الإلكترونية غير موجودة: " + id));
        return toResponse(ei);
    }

    /**
     * Submit e-invoice to government authority.
     */
    public EInvoiceResponse submitToGovernment(Long id) {
        EInvoice ei = eInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الفاتورة الإلكترونية غير موجودة: " + id));

        if (ei.getStatus() != EInvoice.EInvoiceStatus.DRAFT &&
            ei.getStatus() != EInvoice.EInvoiceStatus.REJECTED) {
            throw new BusinessRuleException("لا يمكن تقديم فاتورة بحالة: " + ei.getStatus());
        }

        // Simulate government submission
        ei.setSubmissionId(UUID.randomUUID().toString());
        ei.setStatus(EInvoice.EInvoiceStatus.SUBMITTED);
        ei.setSubmittedAt(Instant.now());
        ei.setUpdatedAt(Instant.now());

        // Simulate acceptance (in production this would be async callback)
        ei.setStatus(EInvoice.EInvoiceStatus.ACCEPTED);
        ei.setResponseData("{\"status\":\"ACCEPTED\",\"message\":\"تم قبول الفاتورة بنجاح\"}");

        ei = eInvoiceRepository.save(ei);
        log.info("Submitted e-invoice {} to government — status: {}", ei.getSerialNumber(), ei.getStatus());
        return toResponse(ei);
    }

    @Transactional(readOnly = true)
    public List<EInvoiceResponse> getPendingInvoices() {
        return eInvoiceRepository.findByStatus(EInvoice.EInvoiceStatus.DRAFT)
                .stream().map(this::toResponse).toList();
    }

    // ── Internal helpers ──

    private EInvoice.EInvoiceFormat resolveFormat(String countryCode) {
        return switch (countryCode.toUpperCase()) {
            case "EG" -> EInvoice.EInvoiceFormat.ETA;
            case "SA" -> EInvoice.EInvoiceFormat.ZATCA;
            case "AE" -> EInvoice.EInvoiceFormat.FTA;
            default -> EInvoice.EInvoiceFormat.ETA;
        };
    }

    private String generateSerialNumber(String countryCode) {
        return "TWS-" + countryCode.toUpperCase() + "-" +
                System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    String buildPayload(Invoice invoice, String countryCode) {
        return "{\"invoiceNumber\":\"" + invoice.getInvoiceNumber() +
                "\",\"country\":\"" + countryCode +
                "\",\"total\":" + invoice.getTotalAmount() +
                ",\"tax\":" + invoice.getTax() + "}";
    }

    String signPayload(String payload) {
        // Simplified signing — in production use HMAC-SHA256 or RSA
        return Base64.getEncoder().encodeToString(
                payload.getBytes(StandardCharsets.UTF_8));
    }

    String generateQrCode(EInvoice ei) {
        String data = ei.getSerialNumber() + "|" + ei.getCountryCode() + "|" + ei.getFormat();
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private EInvoiceResponse toResponse(EInvoice ei) {
        return new EInvoiceResponse(
                ei.getId(),
                ei.getInvoice() != null ? ei.getInvoice().getId() : null,
                ei.getCountryCode(),
                ei.getFormat().name(),
                ei.getSerialNumber(),
                ei.getStatus().name(),
                ei.getSubmissionId(),
                ei.getSubmittedAt() != null ? ei.getSubmittedAt().toString() : null,
                ei.getQrCode());
    }
}
