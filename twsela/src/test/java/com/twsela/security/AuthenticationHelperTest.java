package com.twsela.security;

import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationHelper — user resolution from Authentication")
class AuthenticationHelperTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private AuthenticationHelper authHelper;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role("OWNER");
        role.setId(1L);
        UserStatus status = new UserStatus();
        status.setName("ACTIVE");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Owner");
        testUser.setPhone("0501234567");
        testUser.setRole(role);
        testUser.setStatus(status);
    }

    private Authentication auth(String phone) {
        return new UsernamePasswordAuthenticationToken(phone, null, Collections.emptyList());
    }

    @Test
    @DisplayName("getCurrentUser — found via findByPhoneWithRoleAndStatus")
    void getCurrentUser_foundPrimary() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        User result = authHelper.getCurrentUser(auth("0501234567"));
        assertEquals(testUser, result);
        verify(userRepository).findByPhoneWithRoleAndStatus("0501234567");
    }

    @Test
    @DisplayName("getCurrentUser — fallback to findByPhone")
    void getCurrentUser_fallback() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("0501234567")).thenReturn(Optional.of(testUser));
        User result = authHelper.getCurrentUser(auth("0501234567"));
        assertEquals(testUser, result);
    }

    @Test
    @DisplayName("getCurrentUser — not found throws ResourceNotFoundException")
    void getCurrentUser_notFound() {
        when(userRepository.findByPhoneWithRoleAndStatus("0509999999")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("0509999999")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authHelper.getCurrentUser(auth("0509999999")));
    }

    @Test
    @DisplayName("getCurrentUser — null authentication throws IllegalStateException")
    void getCurrentUser_nullAuth() {
        assertThrows(IllegalStateException.class, () -> authHelper.getCurrentUser(null));
    }

    @Test
    @DisplayName("getCurrentUserId — returns user ID")
    void getCurrentUserId() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        assertEquals(1L, authHelper.getCurrentUserId(auth("0501234567")));
    }

    @Test
    @DisplayName("getCurrentUserRole — returns role name")
    void getCurrentUserRole() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        assertEquals("OWNER", authHelper.getCurrentUserRole(auth("0501234567")));
    }

    @Test
    @DisplayName("hasRole — matches correctly")
    void hasRole_match() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        assertTrue(authHelper.hasRole(auth("0501234567"), "OWNER"));
    }

    @Test
    @DisplayName("hasRole — no match")
    void hasRole_noMatch() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        assertFalse(authHelper.hasRole(auth("0501234567"), "COURIER"));
    }

    @Test
    @DisplayName("hasRole — null auth returns false")
    void hasRole_nullAuth() {
        assertFalse(authHelper.hasRole(null, "OWNER"));
    }

    @Test
    @DisplayName("hasAnyRole — matches one of multiple")
    void hasAnyRole_match() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        assertTrue(authHelper.hasAnyRole(auth("0501234567"), "ADMIN", "OWNER"));
    }

    @Test
    @DisplayName("hasAnyRole — none match")
    void hasAnyRole_noMatch() {
        when(userRepository.findByPhoneWithRoleAndStatus("0501234567")).thenReturn(Optional.of(testUser));
        assertFalse(authHelper.hasAnyRole(auth("0501234567"), "COURIER", "MERCHANT"));
    }
}
