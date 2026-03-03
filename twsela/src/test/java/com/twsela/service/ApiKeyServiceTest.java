package com.twsela.service;

import com.twsela.domain.ApiKey;
import com.twsela.domain.ApiKeyUsageLog;
import com.twsela.domain.User;
import com.twsela.repository.ApiKeyRepository;
import com.twsela.repository.ApiKeyUsageLogRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("اختبارات خدمة مفاتيح API")
class ApiKeyServiceTest {

    @Mock private ApiKeyRepository apiKeyRepository;
    @Mock private ApiKeyUsageLogRepository usageLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ApiKeyService apiKeyService;

    private User merchant;
    private ApiKey apiKey;

    @BeforeEach
    void setUp() {
        merchant = new User();
        merchant.setId(1L);
        merchant.setName("تاجر تجريبي");
        merchant.setPhone("01012345678");

        apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setMerchant(merchant);
        apiKey.setKeyValue("TWS-KEY-ABCDEF1234567890");
        apiKey.setSecretHash("$2a$10$hashedSecret");
        apiKey.setName("مفتاح اختبار");
        apiKey.setScopes("shipments:read,tracking:read");
        apiKey.setRateLimit(100);
        apiKey.setActive(true);
        apiKey.setRequestCount(0L);
        apiKey.setCreatedAt(Instant.now());
    }

    @Nested
    @DisplayName("إنشاء مفتاح API")
    class GenerateApiKey {

        @Test
        @DisplayName("يجب إنشاء مفتاح جديد بنجاح")
        void generateApiKey_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
            when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
                ApiKey k = inv.getArgument(0);
                k.setId(1L);
                return k;
            });

            Map<String, Object> result = apiKeyService.generateApiKey(1L, "مفتاح جديد", "shipments:read");

            assertThat(result).containsKeys("apiKey", "secret");
            ApiKey key = (ApiKey) result.get("apiKey");
            assertThat(key.getKeyValue()).startsWith("TWS-KEY-");
            assertThat(key.getName()).isEqualTo("مفتاح جديد");
            verify(apiKeyRepository).save(any(ApiKey.class));
        }

        @Test
        @DisplayName("يجب رفض إنشاء مفتاح لتاجر غير موجود")
        void generateApiKey_merchantNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> apiKeyService.generateApiKey(99L, "مفتاح", "shipments:read"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("تدوير المفتاح")
    class RotateKey {

        @Test
        @DisplayName("يجب تعطيل المفتاح القديم وإنشاء جديد")
        void rotateKey_success() {
            when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(apiKey));
            when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> {
                ApiKey k = inv.getArgument(0);
                if (k.getId() == null) k.setId(2L);
                return k;
            });
            when(userRepository.findById(1L)).thenReturn(Optional.of(merchant));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newEncoded");

            Map<String, Object> result = apiKeyService.rotateKey(1L);

            assertThat(apiKey.isActive()).isFalse();
            assertThat(result).containsKeys("apiKey", "secret");
            verify(apiKeyRepository, atLeast(2)).save(any(ApiKey.class));
        }
    }

    @Nested
    @DisplayName("إلغاء المفتاح")
    class RevokeKey {

        @Test
        @DisplayName("يجب تعطيل المفتاح بنجاح")
        void revokeKey_success() {
            when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(apiKey));
            when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

            apiKeyService.revokeKey(1L);

            assertThat(apiKey.isActive()).isFalse();
            verify(apiKeyRepository).save(apiKey);
        }
    }

    @Nested
    @DisplayName("التحقق من المفتاح")
    class ValidateKey {

        @Test
        @DisplayName("يجب قبول مفتاح وسر صحيحين")
        void validateKey_valid() {
            when(apiKeyRepository.findByKeyValue("TWS-KEY-ABCDEF1234567890")).thenReturn(Optional.of(apiKey));
            when(passwordEncoder.matches("secret123", "$2a$10$hashedSecret")).thenReturn(true);

            ApiKey result = apiKeyService.validateKey("TWS-KEY-ABCDEF1234567890", "secret123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("يجب رفض مفتاح غير نشط")
        void validateKey_inactive() {
            apiKey.setActive(false);
            when(apiKeyRepository.findByKeyValue("TWS-KEY-ABCDEF1234567890")).thenReturn(Optional.of(apiKey));

            ApiKey result = apiKeyService.validateKey("TWS-KEY-ABCDEF1234567890", "secret");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("يجب رفض مفتاح منتهي الصلاحية")
        void validateKey_expired() {
            apiKey.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
            when(apiKeyRepository.findByKeyValue("TWS-KEY-ABCDEF1234567890")).thenReturn(Optional.of(apiKey));

            ApiKey result = apiKeyService.validateKey("TWS-KEY-ABCDEF1234567890", "secret");
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("حد الاستخدام")
    class RateLimit {

        @Test
        @DisplayName("يجب السماح عند عدم تجاوز الحد")
        void enforceRateLimit_underLimit() {
            when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(apiKey));
            when(usageLogRepository.countByApiKeyIdAndRequestedAtBetween(eq(1L), any(), any())).thenReturn(50L);

            boolean allowed = apiKeyService.enforceRateLimit(1L);

            assertThat(allowed).isTrue();
        }
    }
}
