package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.DocumentManagementDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DocumentTemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final DocumentBatchRepository batchRepository;
    private final DocumentAuditLogRepository auditLogRepository;

    public DocumentTemplateService(DocumentTemplateRepository templateRepository,
                                   GeneratedDocumentRepository generatedDocumentRepository,
                                   DocumentBatchRepository batchRepository,
                                   DocumentAuditLogRepository auditLogRepository) {
        this.templateRepository = templateRepository;
        this.generatedDocumentRepository = generatedDocumentRepository;
        this.batchRepository = batchRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public DocumentTemplateResponse createTemplate(CreateDocumentTemplateRequest request, Long tenantId) {
        DocumentTemplate template = new DocumentTemplate();
        template.setName(request.name());
        template.setType(request.type());
        if (request.format() != null) template.setFormat(request.format());
        template.setTemplateContent(request.templateContent());
        if (request.isDefault() != null) template.setIsDefault(request.isDefault());
        template.setTenantId(tenantId);
        template = templateRepository.save(template);
        return toTemplateResponse(template);
    }

    @Transactional(readOnly = true)
    public DocumentTemplateResponse getTemplateById(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document template not found"));
        return toTemplateResponse(template);
    }

    @Transactional(readOnly = true)
    public List<DocumentTemplateResponse> getTemplatesByType(String type) {
        return templateRepository.findByType(type).stream().map(this::toTemplateResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream().map(this::toTemplateResponse).toList();
    }

    // Generated Document operations
    public GeneratedDocumentResponse generateDocument(GenerateDocumentRequest request, Long tenantId, Long userId) {
        GeneratedDocument doc = new GeneratedDocument();
        doc.setTemplateId(request.templateId());
        doc.setShipmentId(request.shipmentId());
        doc.setDocumentType(request.documentType());
        doc.setFileUrl(request.fileUrl());
        if (request.fileSize() != null) doc.setFileSize(request.fileSize());
        doc.setTenantId(tenantId);
        doc = generatedDocumentRepository.save(doc);

        // Audit log
        DocumentAuditLog audit = new DocumentAuditLog();
        audit.setDocumentId(doc.getId());
        audit.setAction("GENERATED");
        audit.setPerformedById(userId);
        audit.setDetails("Document generated: " + doc.getDocumentType());
        auditLogRepository.save(audit);

        return toGeneratedResponse(doc);
    }

    @Transactional(readOnly = true)
    public List<GeneratedDocumentResponse> getDocumentsByShipment(Long shipmentId) {
        return generatedDocumentRepository.findByShipmentId(shipmentId).stream()
                .map(this::toGeneratedResponse).toList();
    }

    // Batch operations
    public DocumentBatchResponse createBatch(CreateDocumentBatchRequest request, Long tenantId, Long userId) {
        DocumentBatch batch = new DocumentBatch();
        batch.setBatchType(request.batchType());
        if (request.totalDocuments() != null) batch.setTotalDocuments(request.totalDocuments());
        batch.setRequestedById(userId);
        batch.setTenantId(tenantId);
        batch = batchRepository.save(batch);
        return toBatchResponse(batch);
    }

    @Transactional(readOnly = true)
    public DocumentBatchResponse getBatchById(Long id) {
        DocumentBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document batch not found"));
        return toBatchResponse(batch);
    }

    @Transactional(readOnly = true)
    public List<DocumentAuditLogResponse> getAuditLogsByDocument(Long documentId) {
        return auditLogRepository.findByDocumentId(documentId).stream()
                .map(this::toAuditResponse).toList();
    }

    private DocumentTemplateResponse toTemplateResponse(DocumentTemplate t) {
        return new DocumentTemplateResponse(t.getId(), t.getName(), t.getType(), t.getFormat(),
                t.getTemplateContent(), t.getVersion(), t.getIsDefault(), t.getTenantId(), t.getCreatedAt());
    }

    private GeneratedDocumentResponse toGeneratedResponse(GeneratedDocument d) {
        return new GeneratedDocumentResponse(d.getId(), d.getTemplateId(), d.getShipmentId(),
                d.getDocumentType(), d.getFileUrl(), d.getFileSize(), d.getGeneratedAt(),
                d.getExpiresAt(), d.getTenantId(), d.getCreatedAt());
    }

    private DocumentBatchResponse toBatchResponse(DocumentBatch b) {
        return new DocumentBatchResponse(b.getId(), b.getBatchType(), b.getStatus(),
                b.getTotalDocuments(), b.getCompletedDocuments(), b.getRequestedById(),
                b.getStartedAt(), b.getCompletedAt(), b.getTenantId(), b.getCreatedAt());
    }

    private DocumentAuditLogResponse toAuditResponse(DocumentAuditLog a) {
        return new DocumentAuditLogResponse(a.getId(), a.getDocumentId(), a.getAction(),
                a.getPerformedById(), a.getDetails(), a.getCreatedAt());
    }
}
