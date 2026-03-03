package com.twsela.service;

import com.twsela.domain.TenantConfiguration;
import com.twsela.repository.TenantConfigurationRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * خدمة إدارة إعدادات المستأجر.
 */
@Service
@Transactional
public class TenantConfigService {

    private static final Logger log = LoggerFactory.getLogger(TenantConfigService.class);
    private static final String ENCRYPTION_PREFIX = "ENC:";

    private final TenantConfigurationRepository configurationRepository;

    public TenantConfigService(TenantConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    /**
     * جلب إعداد بالمفتاح.
     */
    @Transactional(readOnly = true)
    public TenantConfiguration getConfig(Long tenantId, String configKey) {
        return configurationRepository.findByTenantIdAndConfigKey(tenantId, configKey)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "configKey", configKey));
    }

    /**
     * جلب قيمة إعداد.
     */
    @Transactional(readOnly = true)
    public String getConfigValue(Long tenantId, String configKey) {
        TenantConfiguration config = getConfig(tenantId, configKey);
        if (config.isEncrypted()) {
            return decryptValue(config.getConfigValue());
        }
        return config.getConfigValue();
    }

    /**
     * جلب قيمة إعداد مع قيمة افتراضية.
     */
    @Transactional(readOnly = true)
    public String getConfigValue(Long tenantId, String configKey, String defaultValue) {
        Optional<TenantConfiguration> config = configurationRepository.findByTenantIdAndConfigKey(tenantId, configKey);
        if (config.isEmpty()) {
            return defaultValue;
        }
        TenantConfiguration c = config.get();
        if (c.isEncrypted()) {
            return decryptValue(c.getConfigValue());
        }
        return c.getConfigValue();
    }

    /**
     * تعيين إعداد.
     */
    public TenantConfiguration setConfig(Long tenantId, String configKey, String configValue,
                                          TenantConfiguration.ConfigCategory category,
                                          boolean encrypted, String description) {
        Optional<TenantConfiguration> existing = configurationRepository.findByTenantIdAndConfigKey(tenantId, configKey);

        TenantConfiguration config;
        if (existing.isPresent()) {
            config = existing.get();
        } else {
            config = new TenantConfiguration();
            config.setConfigKey(configKey);
            config.setCategory(category);
            config.setDescription(description);
            // Need to set tenant - we'll use a reference approach
        }

        if (encrypted) {
            config.setConfigValue(encryptValue(configValue));
            config.setEncrypted(true);
        } else {
            config.setConfigValue(configValue);
            config.setEncrypted(false);
        }

        log.info("Config set for tenant {}: key={}, encrypted={}", tenantId, configKey, encrypted);
        return configurationRepository.save(config);
    }

    /**
     * جلب إعدادات حسب الفئة.
     */
    @Transactional(readOnly = true)
    public List<TenantConfiguration> getByCategory(Long tenantId, TenantConfiguration.ConfigCategory category) {
        return configurationRepository.findByTenantIdAndCategory(tenantId, category);
    }

    /**
     * جلب كل إعدادات المستأجر.
     */
    @Transactional(readOnly = true)
    public List<TenantConfiguration> getAllConfigs(Long tenantId) {
        return configurationRepository.findByTenantId(tenantId);
    }

    /**
     * حذف إعداد.
     */
    public void deleteConfig(Long tenantId, String configKey) {
        TenantConfiguration config = getConfig(tenantId, configKey);
        configurationRepository.delete(config);
        log.info("Config deleted for tenant {}: key={}", tenantId, configKey);
    }

    /**
     * تشفير قيمة (Base64 بسيط - في الإنتاج يجب استخدام AES).
     */
    String encryptValue(String value) {
        return ENCRYPTION_PREFIX + Base64.getEncoder().encodeToString(value.getBytes());
    }

    /**
     * فك تشفير قيمة.
     */
    String decryptValue(String encryptedValue) {
        if (encryptedValue != null && encryptedValue.startsWith(ENCRYPTION_PREFIX)) {
            String encoded = encryptedValue.substring(ENCRYPTION_PREFIX.length());
            return new String(Base64.getDecoder().decode(encoded));
        }
        return encryptedValue;
    }
}
