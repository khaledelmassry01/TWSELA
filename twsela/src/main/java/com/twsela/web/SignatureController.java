package com.twsela.web;

import com.twsela.service.SignatureService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DocumentManagementDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/signatures")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<SignatureRequestResponse>> createSignatureRequest(
            @Valid @RequestBody CreateSignatureRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(signatureService.createSignatureRequest(request, tenantId)));
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<SignatureRequestResponse>> getSignatureRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(signatureService.getSignatureRequestById(id)));
    }

    @GetMapping("/requests/token/{token}")
    public ResponseEntity<ApiResponse<SignatureRequestResponse>> getSignatureRequestByToken(@PathVariable String token) {
        return ResponseEntity.ok(ApiResponse.ok(signatureService.getSignatureRequestByToken(token)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DigitalSignatureResponse>> createDigitalSignature(
            @Valid @RequestBody CreateDigitalSignatureRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(signatureService.createDigitalSignature(request)));
    }

    @PostMapping("/customs")
    public ResponseEntity<ApiResponse<CustomsDocumentResponse>> createCustomsDocument(
            @Valid @RequestBody CreateCustomsDocumentRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(signatureService.createCustomsDocument(request, tenantId)));
    }

    @GetMapping("/customs/shipment/{shipmentId}")
    public ResponseEntity<ApiResponse<List<CustomsDocumentResponse>>> getCustomsByShipment(
            @PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok(signatureService.getCustomsDocumentsByShipment(shipmentId)));
    }
}
