package com.twsela.service;

import com.twsela.domain.TenantConfiguration;
import com.twsela.repository.TenantConfigurationRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantConfigServiceTest {

    @Mock private TenantConfigurationRepository configurationRepository;
    @InjectMocks private TenantConfigService configService;

    private TenantConfiguration config;

    @BeforeEach
    void setUp() {
        config = new TenantConfiguration();
        config.setId(1L);
        config.setConfigKey("sms.provider");
        config.setConfigValue("twilio");
        config.setCategory(TenantConfiguration.ConfigCategory.SMS);
        config.setEncrypted(false);
    }

    @Test
    @DisplayName("جلب إعداد بالمفتاح")
    void getConfig_success() {
        when(configurationRepository.findByTenantIdAndConfigKey(1L, "sms.provider"))
                .thenReturn(Optional.of(config));

        TenantConfiguration result = configService.getConfig(1L, "sms.provider");

        assertThat(result.getConfigValue()).isEqualTo("twilio");
    }

    @Test
    @DisplayName("جلب قيمة إعداد عادي")
    void getConfigValue_plainText() {
        when(configurationRepository.findByTenantIdAndConfigKey(1L, "sms.provider"))
                .thenReturn(Optional.of(config));

        String value = configService.getConfigValue(1L, "sms.provider");

        assertThat(value).isEqualTo("twilio");
    }

    @Test
    @DisplayName("جلب قيمة إعداد مشفر")
    void getConfigValue_encrypted() {
        config.setEncrypted(true);
        config.setConfigValue(configService.encryptValue("secret-api-key"));

        when(configurationRepository.findByTenantIdAndConfigKey(1L, "api.key"))
                .thenReturn(Optional.of(config));

        String value = configService.getConfigValue(1L, "api.key");

        assertThat(value).isEqualTo("secret-api-key");
    }

    @Test
    @DisplayName("جلب قيمة افتراضية عند عدم وجود الإعداد")
    void getConfigValue_withDefault() {
        when(configurationRepository.findByTenantIdAndConfigKey(1L, "nonexistent"))
                .thenReturn(Optional.empty());

        String value = configService.getConfigValue(1L, "nonexistent", "default-value");

        assertThat(value).isEqualTo("default-value");
    }

    @Test
    @DisplayName("تعيين إعداد جديد")
    void setConfig_newConfig() {
        when(configurationRepository.findByTenantIdAndConfigKey(1L, "new.key"))
                .thenReturn(Optional.empty());
        when(configurationRepository.save(any(TenantConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantConfiguration result = configService.setConfig(1L, "new.key", "new-value",
                TenantConfiguration.ConfigCategory.GENERAL, false, "وصف الإعداد");

        assertThat(result.getConfigKey()).isEqualTo("new.key");
        assertThat(result.getConfigValue()).isEqualTo("new-value");
    }

    @Test
    @DisplayName("جلب إعدادات حسب الفئة")
    void getByCategory_success() {
        when(configurationRepository.findByTenantIdAndCategory(1L, TenantConfiguration.ConfigCategory.SMS))
                .thenReturn(List.of(config));

        List<TenantConfiguration> result = configService.getByCategory(1L, TenantConfiguration.ConfigCategory.SMS);

        assertThat(result).hasSize(1);
    }
}
