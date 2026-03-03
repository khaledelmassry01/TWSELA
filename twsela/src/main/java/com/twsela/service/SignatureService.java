package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.DocumentManagementDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SignatureService {

    private final SignatureRequestRepository signatureRequestRepository;
    private final DigitalSignatureRepository digitalSignatureRepository;
    private final CustomsDocumentRepository customsDocumentRepository;
    private final DocumentAuditLogRepository auditLogRepository;

    public SignatureService(SignatureRequestRepository signatureRequestRepository,
                           DigitalSignatureRepository digitalSignatureRepository,
                           CustomsDocumentRepository customsDocumentRepository,
                           DocumentAuditLogRepository auditLogRepository) {
        this.signatureRequestRepository = signatureRequestRepository;
        this.digitalSignatureRepository = digitalSignatureRepository;
        this.customsDocumentRepository = customsDocumentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // Signature Request operations
    public SignatureRequestResponse createSignatureRequest(CreateSignatureRequest request, Long tenantId) {
        SignatureRequest sr = new SignatureRequest();
        sr.setDocumentId(request.documentId());
        sr.setSignerName(request.signerName());
        sr.setSignerPhone(request.signerPhone());
        sr.setToken(UUID.randomUUID().toString());
        sr.setExpiresAt(request.expiresAt());
        sr.setTenantId(tenantId);
        sr = signatureRequestRepository.save(sr);
        return toSignatureRequestResponse(sr);
    }

    @Transactional(readOnly = true)
    public SignatureRequestResponse getSignatureRequestById(Long id) {
        SignatureRequest sr = signatureRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Signature request not found"));
        return toSignatureRequestResponse(sr);
    }

    @Transactional(readOnly = true)
    public SignatureRequestResponse getSignatureRequestByToken(String token) {
        SignatureRequest sr = signatureRequestRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Signature request not found"));
        return toSignatureRequestResponse(sr);
    }

    // Digital Signature operations
    public DigitalSignatureResponse createDigitalSignature(CreateDigitalSignatureRequest request) {
        signatureRequestRepository.findById(request.signatureRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Signature request not found"));

        DigitalSignature ds = new DigitalSignature();
        ds.setSignatureRequestId(request.signatureRequestId());
        ds.setSignatureImageUrl(request.signatureImageUrl());
        ds.setIpAddress(request.ipAddress());
        ds.setDeviceInfo(request.deviceInfo());
        ds = digitalSignatureRepository.save(ds);

        // Update request status
        SignatureRequest sr = signatureRequestRepository.findById(request.signatureRequestId()).get();
        sr.setStatus("SIGNED");
        signatureRequestRepository.save(sr);

        return toDigitalSignatureResponse(ds);
    }

    // Customs Document operations
    public CustomsDocumentResponse createCustomsDocument(CreateCustomsDocumentRequest request, Long tenantId) {
        CustomsDocument cd = new CustomsDocument();
        cd.setShipmentId(request.shipmentId());
        cd.setDocumentType(request.documentType());
        cd.setHsCode(request.hsCode());
        cd.setDeclaredValue(request.declaredValue());
        if (request.currency() != null) cd.setCurrency(request.currency());
        cd.setOriginCountry(request.originCountry());
        cd.setDestinationCountry(request.destinationCountry());
        cd.setTenantId(tenantId);
        cd = customsDocumentRepository.save(cd);
        return toCustomsResponse(cd);
    }

    @Transactional(readOnly = true)
    public List<CustomsDocumentResponse> getCustomsDocumentsByShipment(Long shipmentId) {
        return customsDocumentRepository.findByShipmentId(shipmentId).stream()
                .map(this::toCustomsResponse).toList();
    }

    private SignatureRequestResponse toSignatureRequestResponse(SignatureRequest sr) {
        return new SignatureRequestResponse(sr.getId(), sr.getDocumentId(), sr.getSignerName(),
                sr.getSignerPhone(), sr.getStatus(), sr.getToken(), sr.getExpiresAt(),
                sr.getTenantId(), sr.getCreatedAt());
    }

    private DigitalSignatureResponse toDigitalSignatureResponse(DigitalSignature ds) {
        return new DigitalSignatureResponse(ds.getId(), ds.getSignatureRequestId(),
                ds.getSignatureImageUrl(), ds.getSignedAt(), ds.getIpAddress(),
                ds.getDeviceInfo(), ds.getCreatedAt());
    }

    private CustomsDocumentResponse toCustomsResponse(CustomsDocument cd) {
        return new CustomsDocumentResponse(cd.getId(), cd.getShipmentId(), cd.getDocumentType(),
                cd.getHsCode(), cd.getDeclaredValue(), cd.getCurrency(), cd.getOriginCountry(),
                cd.getDestinationCountry(), cd.getTenantId(), cd.getCreatedAt());
    }
}
