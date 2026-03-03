package com.twsela.web.dto;

import com.twsela.domain.TenantBranding;

/**
 * DTOs الخاصة بالعلامة التجارية للمستأجر.
 */
public class TenantBrandingDTO {

    /**
     * طلب تحديث العلامة التجارية.
     */
    public static class BrandingRequest {
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;
        private String fontFamily;
        private String companyNameAr;
        private String companyNameEn;
        private String taglineAr;
        private String taglineEn;
        private String footerText;
        private String customCSS;

        public String getPrimaryColor() { return primaryColor; }
        public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
        public String getSecondaryColor() { return secondaryColor; }
        public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
        public String getAccentColor() { return accentColor; }
        public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
        public String getCompanyNameAr() { return companyNameAr; }
        public void setCompanyNameAr(String companyNameAr) { this.companyNameAr = companyNameAr; }
        public String getCompanyNameEn() { return companyNameEn; }
        public void setCompanyNameEn(String companyNameEn) { this.companyNameEn = companyNameEn; }
        public String getTaglineAr() { return taglineAr; }
        public void setTaglineAr(String taglineAr) { this.taglineAr = taglineAr; }
        public String getTaglineEn() { return taglineEn; }
        public void setTaglineEn(String taglineEn) { this.taglineEn = taglineEn; }
        public String getFooterText() { return footerText; }
        public void setFooterText(String footerText) { this.footerText = footerText; }
        public String getCustomCSS() { return customCSS; }
        public void setCustomCSS(String customCSS) { this.customCSS = customCSS; }
    }

    /**
     * استجابة بيانات العلامة التجارية.
     */
    public static class BrandingResponse {
        private Long id;
        private String logoUrl;
        private String faviconUrl;
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;
        private String fontFamily;
        private String companyNameAr;
        private String companyNameEn;
        private String taglineAr;
        private String taglineEn;
        private String footerText;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        public String getFaviconUrl() { return faviconUrl; }
        public void setFaviconUrl(String faviconUrl) { this.faviconUrl = faviconUrl; }
        public String getPrimaryColor() { return primaryColor; }
        public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
        public String getSecondaryColor() { return secondaryColor; }
        public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
        public String getAccentColor() { return accentColor; }
        public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
        public String getCompanyNameAr() { return companyNameAr; }
        public void setCompanyNameAr(String companyNameAr) { this.companyNameAr = companyNameAr; }
        public String getCompanyNameEn() { return companyNameEn; }
        public void setCompanyNameEn(String companyNameEn) { this.companyNameEn = companyNameEn; }
        public String getTaglineAr() { return taglineAr; }
        public void setTaglineAr(String taglineAr) { this.taglineAr = taglineAr; }
        public String getTaglineEn() { return taglineEn; }
        public void setTaglineEn(String taglineEn) { this.taglineEn = taglineEn; }
        public String getFooterText() { return footerText; }
        public void setFooterText(String footerText) { this.footerText = footerText; }

        public static BrandingResponse from(TenantBranding branding) {
            BrandingResponse resp = new BrandingResponse();
            resp.setId(branding.getId());
            resp.setLogoUrl(branding.getLogoUrl());
            resp.setFaviconUrl(branding.getFaviconUrl());
            resp.setPrimaryColor(branding.getPrimaryColor());
            resp.setSecondaryColor(branding.getSecondaryColor());
            resp.setAccentColor(branding.getAccentColor());
            resp.setFontFamily(branding.getFontFamily());
            resp.setCompanyNameAr(branding.getCompanyNameAr());
            resp.setCompanyNameEn(branding.getCompanyNameEn());
            resp.setTaglineAr(branding.getTaglineAr());
            resp.setTaglineEn(branding.getTaglineEn());
            resp.setFooterText(branding.getFooterText());
            return resp;
        }
    }

    private TenantBrandingDTO() {}
}
