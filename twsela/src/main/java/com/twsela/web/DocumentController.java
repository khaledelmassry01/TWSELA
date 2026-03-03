package com.twsela.web;

import com.twsela.service.DocumentTemplateService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DocumentManagementDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class DocumentController {

    private final DocumentTemplateService templateService;

    public DocumentController(DocumentTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<DocumentTemplateResponse>> createTemplate(
            @Valid @RequestBody CreateDocumentTemplateRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.createTemplate(request, tenantId)));
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<ApiResponse<DocumentTemplateResponse>> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getTemplateById(id)));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<DocumentTemplateResponse>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getAllTemplates()));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GeneratedDocumentResponse>> generateDocument(
            @Valid @RequestBody GenerateDocumentRequest request,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.generateDocument(request, tenantId, userId)));
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<ApiResponse<List<GeneratedDocumentResponse>>> getDocumentsByShipment(
            @PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getDocumentsByShipment(shipmentId)));
    }

    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<DocumentBatchResponse>> createBatch(
            @Valid @RequestBody CreateDocumentBatchRequest request,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.createBatch(request, tenantId, userId)));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<DocumentBatchResponse>> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getBatchById(id)));
    }

    @GetMapping("/audit/{documentId}")
    public ResponseEntity<ApiResponse<List<DocumentAuditLogResponse>>> getAuditLogs(@PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getAuditLogsByDocument(documentId)));
    }
}
