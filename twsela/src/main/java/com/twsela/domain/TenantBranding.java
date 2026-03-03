package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * تخصيص العلامة التجارية للمستأجر.
 */
@Entity
@Table(name = "tenant_branding", indexes = {
        @Index(name = "idx_tenant_branding_tenant", columnList = "tenant_id")
})
public class TenantBranding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    @Column(name = "accent_color", length = 7)
    private String accentColor;

    @Column(name = "font_family", length = 100)
    private String fontFamily;

    @Column(name = "company_name_ar", length = 200)
    private String companyNameAr;

    @Column(name = "company_name_en", length = 200)
    private String companyNameEn;

    @Column(name = "tagline_ar", length = 500)
    private String taglineAr;

    @Column(name = "tagline_en", length = 500)
    private String taglineEn;

    @Column(name = "footer_text", length = 500)
    private String footerText;

    @Column(name = "custom_css", columnDefinition = "TEXT")
    private String customCSS;

    @Column(name = "email_template", columnDefinition = "TEXT")
    private String emailTemplate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

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

    public String getCustomCSS() { return customCSS; }
    public void setCustomCSS(String customCSS) { this.customCSS = customCSS; }

    public String getEmailTemplate() { return emailTemplate; }
    public void setEmailTemplate(String emailTemplate) { this.emailTemplate = emailTemplate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantBranding that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
