package com.twsela.service;

import com.twsela.domain.ApiKey;
import com.twsela.domain.ApiKeyUsageLog;
import com.twsela.domain.User;
import com.twsela.repository.ApiKeyRepository;
import com.twsela.repository.ApiKeyUsageLogRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service managing API keys for merchant developer access.
 */
@Service
@Transactional
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyUsageLogRepository usageLogRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                          ApiKeyUsageLogRepository usageLogRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.apiKeyRepository = apiKeyRepository;
        this.usageLogRepository = usageLogRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generate a new API key for a merchant. Returns a map with the key and the plain secret (shown once).
     */
    public Map<String, Object> generateApiKey(Long merchantId, String name, String scopes) {
        User merchant = userRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", merchantId));

        String keyValue = "TWS-KEY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String plainSecret = UUID.randomUUID().toString().replace("-", "");
        String secretHash = passwordEncoder.encode(plainSecret);

        ApiKey apiKey = new ApiKey();
        apiKey.setMerchant(merchant);
        apiKey.setKeyValue(keyValue);
        apiKey.setSecretHash(secretHash);
        apiKey.setName(name);
        apiKey.setScopes(scopes != null ? scopes : "shipments:read,tracking:read");
        apiKey.setRateLimit(100); // Default, can be updated per subscription
        apiKey.setActive(true);

        apiKey = apiKeyRepository.save(apiKey);
        log.info("API key {} generated for merchant {}", keyValue, merchantId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("apiKey", apiKey);
        result.put("secret", plainSecret); // Only returned once
        return result;
    }

    /**
     * Rotate key — deactivate old and generate new.
     */
    public Map<String, Object> rotateKey(Long keyId) {
        ApiKey oldKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", "id", keyId));

        oldKey.setActive(false);
        apiKeyRepository.save(oldKey);
        log.info("API key {} deactivated (rotation)", oldKey.getKeyValue());

        return generateApiKey(oldKey.getMerchant().getId(), oldKey.getName() + " (rotated)", oldKey.getScopes());
    }

    /**
     * Revoke/deactivate a key.
     */
    public void revokeKey(Long keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", "id", keyId));
        key.setActive(false);
        apiKeyRepository.save(key);
        log.info("API key {} revoked", key.getKeyValue());
    }

    /**
     * Validate an API key + secret combination. Returns the ApiKey if valid, else null.
     */
    @Transactional(readOnly = true)
    public ApiKey validateKey(String keyValue, String secret) {
        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyValue(keyValue);
        if (keyOpt.isEmpty()) return null;

        ApiKey key = keyOpt.get();
        if (!key.isActive()) return null;
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(Instant.now())) return null;
        if (!passwordEncoder.matches(secret, key.getSecretHash())) return null;

        return key;
    }

    /**
     * Get keys by merchant.
     */
    @Transactional(readOnly = true)
    public List<ApiKey> getKeysByMerchant(Long merchantId) {
        return apiKeyRepository.findByMerchantId(merchantId);
    }

    /**
     * Get usage statistics for a key.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUsageStats(Long keyId, Instant from, Instant to) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", "id", keyId));

        long totalRequests = usageLogRepository.countByApiKeyIdAndRequestedAtBetween(keyId, from, to);
        List<ApiKeyUsageLog> recentLogs = usageLogRepository.findByApiKeyIdOrderByRequestedAtDesc(keyId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("keyId", keyId);
        stats.put("keyValue", key.getKeyValue());
        stats.put("totalRequests", totalRequests);
        stats.put("totalLifetimeRequests", key.getRequestCount());
        stats.put("recentCalls", recentLogs.size() > 10 ? recentLogs.subList(0, 10) : recentLogs);
        return stats;
    }

    /**
     * Check rate limit: returns true if under limit, false if exceeded.
     * Uses simple DB-based counting (Redis-backed in production).
     */
    @Transactional(readOnly = true)
    public boolean enforceRateLimit(Long keyId) {
        ApiKey key = apiKeyRepository.findById(keyId).orElse(null);
        if (key == null) return false;

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long count = usageLogRepository.countByApiKeyIdAndRequestedAtBetween(keyId, oneHourAgo, Instant.now());
        return count < key.getRateLimit();
    }

    /**
     * Record API usage.
     */
    public void recordUsage(Long apiKeyId, String endpoint, String method,
                             int responseStatus, String ipAddress, String userAgent) {
        ApiKeyUsageLog usageLog = new ApiKeyUsageLog();
        usageLog.setApiKeyId(apiKeyId);
        usageLog.setEndpoint(endpoint);
        usageLog.setMethod(method);
        usageLog.setResponseStatus(responseStatus);
        usageLog.setIpAddress(ipAddress);
        usageLog.setUserAgent(userAgent);
        usageLogRepository.save(usageLog);

        // Update key stats
        apiKeyRepository.findById(apiKeyId).ifPresent(key -> {
            key.setLastUsedAt(Instant.now());
            key.setRequestCount(key.getRequestCount() + 1);
            apiKeyRepository.save(key);
        });
    }
}
