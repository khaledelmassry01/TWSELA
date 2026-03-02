package com.twsela.security;

import com.twsela.domain.User;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Helper component to uniformly resolve the current authenticated user.
 * Replaces scattered (User) authentication.getPrincipal() / authentication.getName() patterns.
 */
@Component
public class AuthenticationHelper {

    private final UserRepository userRepository;

    public AuthenticationHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolve the full User entity for the current authenticated user.
     *
     * @throws ResourceNotFoundException if the user no longer exists in the database
     * @throws IllegalStateException if authentication is null or not authenticated
     */
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        String phone = authentication.getName();
        return userRepository.findByPhoneWithRoleAndStatus(phone)
                .or(() -> userRepository.findByPhone(phone))
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", phone));
    }

    public Long getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getId();
    }

    public String getCurrentUserRole(Authentication authentication) {
        return getCurrentUser(authentication).getRole().getName();
    }

    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        return role.equalsIgnoreCase(getCurrentUserRole(authentication));
    }

    public boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null) return false;
        String userRole = getCurrentUserRole(authentication);
        for (String role : roles) {
            if (role.equalsIgnoreCase(userRole)) return true;
        }
        return false;
    }
}
