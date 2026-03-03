package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DocumentManagementDTO {

    private DocumentManagementDTO() {}

    // DocumentTemplate DTOs
    public record CreateDocumentTemplateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 30) String type,
        String format,
        String templateContent,
        Boolean isDefault
    ) {}

    public record DocumentTemplateResponse(
        Long id, String name, String type, String format,
        String templateContent, Integer version, Boolean isDefault,
        Long tenantId, LocalDateTime createdAt
    ) {}

    // GeneratedDocument DTOs
    public record GenerateDocumentRequest(
        Long templateId,
        Long shipmentId,
        @NotBlank @Size(max = 30) String documentType,
        String fileUrl,
        Long fileSize
    ) {}

    public record GeneratedDocumentResponse(
        Long id, Long templateId, Long shipmentId, String documentType,
        String fileUrl, Long fileSize, LocalDateTime generatedAt,
        LocalDateTime expiresAt, Long tenantId, LocalDateTime createdAt
    ) {}

    // DocumentBatch DTOs
    public record CreateDocumentBatchRequest(
        @NotBlank @Size(max = 30) String batchType,
        Integer totalDocuments
    ) {}

    public record DocumentBatchResponse(
        Long id, String batchType, String status, Integer totalDocuments,
        Integer completedDocuments, Long requestedById,
        LocalDateTime startedAt, LocalDateTime completedAt,
        Long tenantId, LocalDateTime createdAt
    ) {}

    // SignatureRequest DTOs
    public record CreateSignatureRequest(
        Long documentId,
        @NotBlank @Size(max = 100) String signerName,
        String signerPhone,
        @NotNull LocalDateTime expiresAt
    ) {}

    public record SignatureRequestResponse(
        Long id, Long documentId, String signerName, String signerPhone,
        String status, String token, LocalDateTime expiresAt,
        Long tenantId, LocalDateTime createdAt
    ) {}

    // DigitalSignature DTOs
    public record CreateDigitalSignatureRequest(
        @NotNull Long signatureRequestId,
        String signatureImageUrl,
        String ipAddress,
        String deviceInfo
    ) {}

    public record DigitalSignatureResponse(
        Long id, Long signatureRequestId, String signatureImageUrl,
        LocalDateTime signedAt, String ipAddress, String deviceInfo,
        LocalDateTime createdAt
    ) {}

    // CustomsDocument DTOs
    public record CreateCustomsDocumentRequest(
        @NotNull Long shipmentId,
        @NotBlank @Size(max = 30) String documentType,
        String hsCode,
        BigDecimal declaredValue,
        String currency,
        String originCountry,
        String destinationCountry
    ) {}

    public record CustomsDocumentResponse(
        Long id, Long shipmentId, String documentType, String hsCode,
        BigDecimal declaredValue, String currency, String originCountry,
        String destinationCountry, Long tenantId, LocalDateTime createdAt
    ) {}

    // DocumentAuditLog DTOs
    public record DocumentAuditLogResponse(
        Long id, Long documentId, String action, Long performedById,
        String details, LocalDateTime createdAt
    ) {}
}
