package com.twsela.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService — Redis-based token revocation")
class TokenBlacklistServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private JwtService jwtService;
    @Mock private ValueOperations<String, String> valueOps;

    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService(redisTemplate, jwtService);
    }

    @Test
    @DisplayName("blacklist — stores token in Redis with TTL")
    @SuppressWarnings("unchecked")
    void blacklist_storesWithTtl() {
        String token = "test.jwt.token";
        Date futureDate = new Date(System.currentTimeMillis() + 60_000);
        when(jwtService.extractClaim(eq(token), any(Function.class))).thenReturn(futureDate);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.blacklist(token);

        verify(valueOps).set(eq("jwt:blacklist:" + token), eq("revoked"), any(Duration.class));
    }

    @Test
    @DisplayName("blacklist — expired token (ttl <= 0) not stored")
    @SuppressWarnings("unchecked")
    void blacklist_expiredToken_notStored() {
        String token = "expired.jwt.token";
        Date pastDate = new Date(System.currentTimeMillis() - 60_000);
        when(jwtService.extractClaim(eq(token), any(Function.class))).thenReturn(pastDate);

        service.blacklist(token);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("blacklist — exception does not propagate")
    @SuppressWarnings("unchecked")
    void blacklist_exception_noPropagation() {
        when(jwtService.extractClaim(anyString(), any(Function.class))).thenThrow(new RuntimeException("parse error"));
        assertDoesNotThrow(() -> service.blacklist("bad.token"));
    }

    @Test
    @DisplayName("isBlacklisted — returns true when key exists")
    void isBlacklisted_true() {
        when(redisTemplate.hasKey("jwt:blacklist:revoked.token")).thenReturn(true);
        assertTrue(service.isBlacklisted("revoked.token"));
    }

    @Test
    @DisplayName("isBlacklisted — returns false when key absent")
    void isBlacklisted_false() {
        when(redisTemplate.hasKey("jwt:blacklist:valid.token")).thenReturn(false);
        assertFalse(service.isBlacklisted("valid.token"));
    }

    @Test
    @DisplayName("isBlacklisted — Redis exception returns false")
    void isBlacklisted_exception() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));
        assertFalse(service.isBlacklisted("any.token"));
    }
}
