package com.twsela.service;

import com.twsela.domain.TenantBranding;
import com.twsela.repository.TenantBrandingRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * خدمة إدارة العلامة التجارية للمستأجر.
 */
@Service
@Transactional
public class TenantBrandingService {

    private static final Logger log = LoggerFactory.getLogger(TenantBrandingService.class);

    private final TenantBrandingRepository tenantBrandingRepository;

    public TenantBrandingService(TenantBrandingRepository tenantBrandingRepository) {
        this.tenantBrandingRepository = tenantBrandingRepository;
    }

    /**
     * جلب العلامة التجارية للمستأجر.
     */
    @Transactional(readOnly = true)
    public TenantBranding getByTenantId(Long tenantId) {
        return tenantBrandingRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TenantBranding", "tenantId", tenantId));
    }

    /**
     * تحديث العلامة التجارية.
     */
    public TenantBranding updateBranding(Long tenantId, String primaryColor, String secondaryColor,
                                          String accentColor, String fontFamily,
                                          String companyNameAr, String companyNameEn,
                                          String taglineAr, String taglineEn,
                                          String footerText, String customCSS) {
        TenantBranding branding = getByTenantId(tenantId);
        if (primaryColor != null) branding.setPrimaryColor(primaryColor);
        if (secondaryColor != null) branding.setSecondaryColor(secondaryColor);
        if (accentColor != null) branding.setAccentColor(accentColor);
        if (fontFamily != null) branding.setFontFamily(fontFamily);
        if (companyNameAr != null) branding.setCompanyNameAr(companyNameAr);
        if (companyNameEn != null) branding.setCompanyNameEn(companyNameEn);
        if (taglineAr != null) branding.setTaglineAr(taglineAr);
        if (taglineEn != null) branding.setTaglineEn(taglineEn);
        if (footerText != null) branding.setFooterText(footerText);
        if (customCSS != null) branding.setCustomCSS(customCSS);

        log.info("Branding updated for tenant {}", tenantId);
        return tenantBrandingRepository.save(branding);
    }

    /**
     * رفع شعار.
     */
    public TenantBranding uploadLogo(Long tenantId, String logoUrl) {
        TenantBranding branding = getByTenantId(tenantId);
        branding.setLogoUrl(logoUrl);
        log.info("Logo uploaded for tenant {}: {}", tenantId, logoUrl);
        return tenantBrandingRepository.save(branding);
    }

    /**
     * إنشاء ثيم CSS ديناميكي.
     */
    @Transactional(readOnly = true)
    public String generateCSS(Long tenantId) {
        TenantBranding branding = getByTenantId(tenantId);
        StringBuilder css = new StringBuilder();
        css.append(":root {\n");
        if (branding.getPrimaryColor() != null)
            css.append("  --primary-color: ").append(branding.getPrimaryColor()).append(";\n");
        if (branding.getSecondaryColor() != null)
            css.append("  --secondary-color: ").append(branding.getSecondaryColor()).append(";\n");
        if (branding.getAccentColor() != null)
            css.append("  --accent-color: ").append(branding.getAccentColor()).append(";\n");
        if (branding.getFontFamily() != null)
            css.append("  --font-family: ").append(branding.getFontFamily()).append(";\n");
        css.append("}\n");
        if (branding.getCustomCSS() != null) {
            css.append(branding.getCustomCSS());
        }
        return css.toString();
    }
}
