package com.twsela.service;

import com.twsela.domain.PartnerHandoff;
import com.twsela.domain.ThirdPartyPartner;
import com.twsela.repository.PartnerHandoffRepository;
import com.twsela.repository.ThirdPartyPartnerRepository;
import com.twsela.web.dto.MultiCarrierDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ThirdPartyPartnerService {

    private final ThirdPartyPartnerRepository partnerRepository;
    private final PartnerHandoffRepository handoffRepository;

    public ThirdPartyPartnerService(ThirdPartyPartnerRepository partnerRepository,
                                     PartnerHandoffRepository handoffRepository) {
        this.partnerRepository = partnerRepository;
        this.handoffRepository = handoffRepository;
    }

    // === Partners ===

    public ThirdPartyPartnerResponse createPartner(CreatePartnerRequest request, Long tenantId) {
        ThirdPartyPartner p = new ThirdPartyPartner();
        p.setName(request.name());
        p.setContactPhone(request.contactPhone());
        p.setServiceArea(request.serviceArea());
        if (request.commissionRate() != null) p.setCommissionRate(request.commissionRate());
        p.setTenantId(tenantId);
        return toPartnerResponse(partnerRepository.save(p));
    }

    @Transactional(readOnly = true)
    public ThirdPartyPartnerResponse getPartnerById(Long id) {
        return toPartnerResponse(partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<ThirdPartyPartnerResponse> getActivePartners(Long tenantId) {
        return partnerRepository.findByTenantIdAndStatusOrderByNameAsc(tenantId, "ACTIVE")
                .stream().map(this::toPartnerResponse).toList();
    }

    // === Handoffs ===

    public PartnerHandoffResponse createHandoff(CreatePartnerHandoffRequest request, Long tenantId) {
        PartnerHandoff h = new PartnerHandoff();
        h.setShipmentId(request.shipmentId());
        h.setPartnerId(request.partnerId());
        h.setPartnerTrackingNumber(request.partnerTrackingNumber());
        h.setTenantId(tenantId);
        return toHandoffResponse(handoffRepository.save(h));
    }

    @Transactional(readOnly = true)
    public List<PartnerHandoffResponse> getHandoffsByPartner(Long partnerId) {
        return handoffRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId)
                .stream().map(this::toHandoffResponse).toList();
    }

    public PartnerHandoffResponse updateHandoffStatus(Long id, String status) {
        PartnerHandoff h = handoffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Handoff not found: " + id));
        h.setStatus(status);
        return toHandoffResponse(handoffRepository.save(h));
    }

    // === Mappers ===

    private ThirdPartyPartnerResponse toPartnerResponse(ThirdPartyPartner p) {
        return new ThirdPartyPartnerResponse(p.getId(), p.getName(), p.getContactPhone(),
                p.getServiceArea(), p.getCommissionRate(), p.getStatus(), p.getTenantId(), p.getCreatedAt());
    }

    private PartnerHandoffResponse toHandoffResponse(PartnerHandoff h) {
        return new PartnerHandoffResponse(h.getId(), h.getShipmentId(), h.getPartnerId(),
                h.getHandoffDate(), h.getStatus(), h.getPartnerTrackingNumber(), h.getTenantId(), h.getCreatedAt());
    }
}
