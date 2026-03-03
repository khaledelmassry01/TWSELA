package com.twsela.config;

import com.twsela.domain.ApiKey;
import com.twsela.domain.User;
import com.twsela.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("اختبارات فلتر مصادقة API Key")
class ApiKeyAuthFilterTest {

    @Mock private ApiKeyService apiKeyService;
    @Mock private FilterChain filterChain;

    @InjectMocks private ApiKeyAuthFilter apiKeyAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ApiKey apiKey;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v2/shipments");
        response = new MockHttpServletResponse();

        User merchant = new User();
        merchant.setId(1L);
        merchant.setPhone("01012345678");

        apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setMerchant(merchant);
        apiKey.setKeyValue("TWS-KEY-TEST123");
        apiKey.setActive(true);
        apiKey.setRateLimit(100);
        apiKey.setRequestCount(0L);
        apiKey.setCreatedAt(Instant.now());
    }

    @Nested
    @DisplayName("تصفية المسارات")
    class PathFiltering {

        @Test
        @DisplayName("يجب تخطي المسارات غير /api/v2/")
        void shouldNotFilter_nonV2Paths() {
            request.setRequestURI("/api/shipments");
            boolean result = apiKeyAuthFilter.shouldNotFilter(request);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("يجب تطبيق الفلتر على /api/v2/")
        void shouldFilter_v2Paths() {
            request.setRequestURI("/api/v2/shipments");
            boolean result = apiKeyAuthFilter.shouldNotFilter(request);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("المصادقة بالمفتاح")
    class Authentication {

        @Test
        @DisplayName("يجب المصادقة بنجاح بمفتاح صحيح")
        void authenticate_validKey() throws Exception {
            request.addHeader("X-API-Key", "TWS-KEY-TEST123");
            request.addHeader("X-API-Secret", "secret123");
            when(apiKeyService.validateKey("TWS-KEY-TEST123", "secret123")).thenReturn(apiKey);
            when(apiKeyService.enforceRateLimit(1L)).thenReturn(true);

            apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            verify(filterChain).doFilter(request, response);
            verify(apiKeyService).recordUsage(eq(1L), anyString(), anyString(), anyInt(), any(), any());
        }

        @Test
        @DisplayName("يجب رفض مفتاح غير صحيح")
        void authenticate_invalidKey() throws Exception {
            request.addHeader("X-API-Key", "INVALID");
            request.addHeader("X-API-Secret", "wrong");
            when(apiKeyService.validateKey("INVALID", "wrong")).thenReturn(null);

            apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("يجب رفض الطلب عند تجاوز حد الاستخدام")
        void authenticate_rateLimitExceeded() throws Exception {
            request.addHeader("X-API-Key", "TWS-KEY-TEST123");
            request.addHeader("X-API-Secret", "secret123");
            when(apiKeyService.validateKey("TWS-KEY-TEST123", "secret123")).thenReturn(apiKey);
            when(apiKeyService.enforceRateLimit(1L)).thenReturn(false);

            apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

            assertThat(response.getStatus()).isEqualTo(429);
            verify(filterChain, never()).doFilter(request, response);
        }
    }
}
