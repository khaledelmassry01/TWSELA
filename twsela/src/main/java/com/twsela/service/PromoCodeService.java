package com.twsela.service;

import com.twsela.domain.PromoCode;
import com.twsela.repository.PromoCodeRepository;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    public PromoCodeResponse createPromoCode(CreatePromoCodeRequest request, Long createdById, Long tenantId) {
        if (promoCodeRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Promo code already exists: " + request.code());
        }
        PromoCode pc = new PromoCode();
        pc.setCode(request.code());
        pc.setName(request.name());
        pc.setNameAr(request.nameAr());
        pc.setDiscountType(request.discountType());
        pc.setDiscountValue(request.discountValue());
        pc.setMinOrderValue(request.minOrderValue());
        pc.setMaxDiscountAmount(request.maxDiscountAmount());
        pc.setMaxUsageTotal(request.maxUsageTotal());
        pc.setMaxUsagePerUser(request.maxUsagePerUser());
        pc.setValidFrom(request.validFrom());
        pc.setValidUntil(request.validUntil());
        pc.setApplicableZones(request.applicableZones());
        pc.setApplicablePlans(request.applicablePlans());
        pc.setCreatedById(createdById);
        pc.setTenantId(tenantId);
        return toResponse(promoCodeRepository.save(pc));
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse getByCode(String code) {
        PromoCode pc = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + code));
        return toResponse(pc);
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse getById(Long id) {
        PromoCode pc = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + id));
        return toResponse(pc);
    }

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getActivePromoCodes(Long tenantId) {
        return promoCodeRepository.findByTenantIdAndIsActiveTrueOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PromoCodeResponse> getAllByTenant(Long tenantId) {
        return promoCodeRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    public PromoCodeResponse deactivate(Long id) {
        PromoCode pc = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + id));
        pc.setIsActive(false);
        return toResponse(promoCodeRepository.save(pc));
    }

    private PromoCodeResponse toResponse(PromoCode pc) {
        return new PromoCodeResponse(pc.getId(), pc.getCode(), pc.getName(), pc.getNameAr(),
                pc.getDiscountType(), pc.getDiscountValue(), pc.getMinOrderValue(), pc.getMaxDiscountAmount(),
                pc.getMaxUsageTotal(), pc.getMaxUsagePerUser(), pc.getCurrentUsage(),
                pc.getValidFrom(), pc.getValidUntil(), pc.getApplicableZones(), pc.getApplicablePlans(),
                pc.getIsActive(), pc.getCreatedById(), pc.getTenantId(), pc.getCreatedAt());
    }
}
