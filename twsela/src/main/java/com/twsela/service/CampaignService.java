package com.twsela.service;

import com.twsela.domain.Campaign;
import com.twsela.repository.CampaignRepository;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public CampaignResponse createCampaign(CreateCampaignRequest request, Long tenantId) {
        Campaign c = new Campaign();
        c.setName(request.name());
        c.setNameAr(request.nameAr());
        c.setDescription(request.description());
        c.setCampaignType(request.campaignType());
        c.setTargetAudience(request.targetAudience());
        c.setTargetCriteria(request.targetCriteria());
        c.setPromoCodeId(request.promoCodeId());
        c.setMessage(request.message());
        c.setMessageAr(request.messageAr());
        c.setChannel(request.channel() != null ? request.channel() : "ALL");
        c.setTenantId(tenantId);
        return toResponse(campaignRepository.save(c));
    }

    @Transactional(readOnly = true)
    public CampaignResponse getById(Long id) {
        Campaign c = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByTenant(Long tenantId) {
        return campaignRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByStatus(Long tenantId, String status) {
        return campaignRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status)
                .stream().map(this::toResponse).toList();
    }

    public CampaignResponse launchCampaign(Long id) {
        Campaign c = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
        c.setStatus("ACTIVE");
        c.setStartedAt(LocalDateTime.now());
        return toResponse(campaignRepository.save(c));
    }

    public CampaignResponse completeCampaign(Long id) {
        Campaign c = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
        c.setStatus("COMPLETED");
        c.setCompletedAt(LocalDateTime.now());
        return toResponse(campaignRepository.save(c));
    }

    private CampaignResponse toResponse(Campaign c) {
        return new CampaignResponse(c.getId(), c.getName(), c.getNameAr(), c.getDescription(),
                c.getCampaignType(), c.getTargetAudience(), c.getTargetCriteria(), c.getPromoCodeId(),
                c.getMessage(), c.getMessageAr(), c.getChannel(), c.getStatus(),
                c.getScheduledAt(), c.getStartedAt(), c.getCompletedAt(),
                c.getTotalTargets(), c.getTotalSent(), c.getTotalOpened(), c.getTotalConverted(),
                c.getTenantId(), c.getCreatedAt());
    }
}
