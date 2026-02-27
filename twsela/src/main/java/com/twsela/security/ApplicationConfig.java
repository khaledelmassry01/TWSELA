package com.twsela.security;

import com.twsela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class ApplicationConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    private final UserRepository userRepository;

    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                com.twsela.domain.User user = userRepository.findByPhoneWithRoleAndStatus(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + username));
                
                // CRITICAL FIX: Add null checks to prevent NullPointerException
                if (user.getRole() == null) {
                    throw new UsernameNotFoundException("User role not found for phone: " + username);
                }
                
                if (user.getStatus() == null) {
                    throw new UsernameNotFoundException("User status not found for phone: " + username);
                }
                
                // This assumes you have a Role entity with a getName() method
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
                
                // Check if user is active
                boolean isEnabled = user.getStatus() != null && "ACTIVE".equals(user.getStatus().getName()) && !user.getIsDeleted();

                return new org.springframework.security.core.userdetails.User(
                        user.getPhone(), user.getPassword(), isEnabled, true, true, true, authorities);
            } catch (Exception e) {
                log.error("Error loading user details for: {} - {}", username, e.getMessage(), e);
                throw new UsernameNotFoundException("User not found with phone: " + username);
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
