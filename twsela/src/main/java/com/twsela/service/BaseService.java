package com.twsela.service;

import com.twsela.domain.User;
import com.twsela.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base service with common functionality
 * Consolidates duplicate code across services
 */
public abstract class BaseService {
    
    @Autowired
    protected UserRepository userRepository;
    
    /**
     * Find user by phone with error handling
     * Consolidates duplicate user lookup logic
     */
    protected User findUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
    }
    
    /**
     * Validate user exists and is active
     * Consolidates user validation logic
     */
    protected void validateUser(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!user.isActive()) {
            throw new RuntimeException("User account is not active");
        }
    }
    
    /**
     * Validate user role
     * Consolidates role validation logic
     */
    protected void validateUserRole(User user, String expectedRole) {
        if (!user.getRole().getName().equals(expectedRole)) {
            throw new RuntimeException("User does not have required role: " + expectedRole);
        }
    }
}
