package com.twsela.service;

import com.twsela.domain.TenantBranding;
import com.twsela.repository.TenantBrandingRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantBrandingServiceTest {

    @Mock private TenantBrandingRepository brandingRepository;
    @InjectMocks private TenantBrandingService brandingService;

    private TenantBranding branding;

    @BeforeEach
    void setUp() {
        branding = new TenantBranding();
        branding.setId(1L);
        branding.setPrimaryColor("#1a73e8");
        branding.setSecondaryColor("#ffffff");
        branding.setAccentColor("#ff6d00");
        branding.setFontFamily("Noto Sans Arabic");
        branding.setCompanyNameAr("شركة التوصيل");
        branding.setCompanyNameEn("Delivery Co");
    }

    @Test
    @DisplayName("جلب العلامة التجارية بنجاح")
    void getByTenantId_success() {
        when(brandingRepository.findByTenantId(1L)).thenReturn(Optional.of(branding));

        TenantBranding result = brandingService.getByTenantId(1L);

        assertThat(result.getPrimaryColor()).isEqualTo("#1a73e8");
    }

    @Test
    @DisplayName("خطأ عند عدم وجود العلامة التجارية")
    void getByTenantId_notFound() {
        when(brandingRepository.findByTenantId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandingService.getByTenantId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("تحديث العلامة التجارية")
    void updateBranding_success() {
        when(brandingRepository.findByTenantId(1L)).thenReturn(Optional.of(branding));
        when(brandingRepository.save(any(TenantBranding.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantBranding result = brandingService.updateBranding(1L, "#000000", "#111111",
                "#222222", "Cairo", "اسم جديد", "New Name",
                "شعار", "Tagline", "Footer", "body{}");

        assertThat(result.getPrimaryColor()).isEqualTo("#000000");
        assertThat(result.getCompanyNameAr()).isEqualTo("اسم جديد");
    }

    @Test
    @DisplayName("رفع شعار")
    void uploadLogo_success() {
        when(brandingRepository.findByTenantId(1L)).thenReturn(Optional.of(branding));
        when(brandingRepository.save(any(TenantBranding.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantBranding result = brandingService.uploadLogo(1L, "https://example.com/logo.png");

        assertThat(result.getLogoUrl()).isEqualTo("https://example.com/logo.png");
    }

    @Test
    @DisplayName("إنشاء CSS ديناميكي")
    void generateCSS_success() {
        when(brandingRepository.findByTenantId(1L)).thenReturn(Optional.of(branding));

        String css = brandingService.generateCSS(1L);

        assertThat(css).contains("--primary-color: #1a73e8");
        assertThat(css).contains("--secondary-color: #ffffff");
        assertThat(css).contains("--accent-color: #ff6d00");
        assertThat(css).contains("--font-family: Noto Sans Arabic");
    }
}
