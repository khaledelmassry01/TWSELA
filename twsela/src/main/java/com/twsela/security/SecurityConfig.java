package com.twsela.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final RateLimitFilter rateLimitFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(AuthenticationProvider authenticationProvider, 
                         JwtAuthenticationFilter jwtAuthFilter,
                         SecurityExceptionHandler securityExceptionHandler,
                         RateLimitFilter rateLimitFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
        this.securityExceptionHandler = securityExceptionHandler;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // This line enables the CORS configuration defined in the bean below
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // CORS preflight requests - must be first
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints - no authentication required
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/login", "/api/v1/auth/**", "/api/public/**").permitAll()
                // Swagger/OpenAPI documentation endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                // WebSocket endpoint (auth handled by STOMP interceptor)
                .requestMatchers("/ws/**").permitAll()
                // Static files - no authentication required
                .requestMatchers("/", "/index.html", "/login.html", "/contact.html", "/profile.html", "/settings.html", "/404.html").permitAll()
                .requestMatchers("/frontend/**", "/static/**", "/*.html", "/*/*.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.ico", "/*.svg", "/src/**").permitAll()
                .requestMatchers("/merchant/**", "/owner/**", "/courier/**", "/warehouse/**", "/admin/**").permitAll()
                // Actuator endpoints for monitoring — only health/info public
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics", "/actuator/prometheus").hasAnyRole("OWNER", "ADMIN")
                // Authentication required for /api/auth/me and /api/v1/auth/me
                .requestMatchers("/api/auth/me", "/api/v1/auth/me").authenticated()
                // All other auth endpoints are open for registration, password reset, etc.
                .requestMatchers("/api/auth/**", "/api/v1/auth/**").permitAll()
                // Unified endpoints with role-based access control
                // Courier location tracking
                .requestMatchers("/api/couriers/location").hasRole("COURIER")
                .requestMatchers("/api/couriers/*/location/**", "/api/couriers/*/location").hasAnyRole("OWNER", "ADMIN", "COURIER")
                // Returns management
                .requestMatchers("/api/returns/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER")
                // Wallet management
                .requestMatchers("/api/wallet/admin/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/wallet/**").authenticated()
                // Webhooks
                .requestMatchers("/api/webhooks/retry").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/webhooks/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT")
                // Analytics
                .requestMatchers("/api/analytics/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/bi-analytics/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/shipments/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/ratings/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT")
                .requestMatchers("/api/master/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/dashboard/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/manifests/**").hasAnyRole("OWNER", "ADMIN", "COURIER")
                .requestMatchers("/api/financial/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER")
                .requestMatchers("/api/reports/export/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/reports/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/warehouse/**").hasAnyRole("OWNER", "ADMIN", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/settings/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                // Subscriptions & Billing
                .requestMatchers(HttpMethod.GET, "/api/subscriptions/plans").permitAll()
                .requestMatchers("/api/subscriptions/**").hasRole("MERCHANT")
                .requestMatchers("/api/invoices/admin/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/invoices/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT")
                .requestMatchers("/api/payment/webhook/**").permitAll()
                // Fleet Management
                .requestMatchers("/api/fleet/**").hasAnyRole("OWNER", "ADMIN", "COURIER")
                // Support & Knowledge Base
                .requestMatchers(HttpMethod.GET, "/api/support/articles/**").permitAll()
                .requestMatchers("/api/support/tickets/admin/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/support/tickets/**").authenticated()
                .requestMatchers("/api/support/articles/**").hasAnyRole("OWNER", "ADMIN")
                // Smart Assignment & Routes & Demand
                .requestMatchers("/api/assignment/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/routes/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/demand/**").hasAnyRole("OWNER", "ADMIN")
                // Multi-Country & Internationalization
                .requestMatchers(HttpMethod.GET, "/api/countries/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/currencies/**").permitAll()
                .requestMatchers("/api/admin/countries/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/admin/currencies/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/tax/calculate").authenticated()
                .requestMatchers("/api/admin/tax/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/admin/einvoice/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/contact/**").permitAll()
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/api/statistics/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/audit/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/sms/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/backup/**").hasAnyRole("OWNER", "ADMIN")
                // Delivery Proof & Attempts
                .requestMatchers("/api/delivery/admin/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/delivery/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER")
                // Pickup Scheduling
                .requestMatchers("/api/pickups/admin/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/pickups/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER")
                // Advanced Notifications (Sprint 27)
                .requestMatchers("/api/admin/notifications/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/notifications/preferences/**").authenticated()
                .requestMatchers("/api/notifications/devices/**").authenticated()
                // Contracts & Custom Pricing (Sprint 29)
                .requestMatchers("/api/admin/contracts/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/contracts/my").authenticated()
                .requestMatchers("/api/contracts/*/sign").authenticated()
                .requestMatchers("/api/pricing/calculate").authenticated()
                // API Platform & E-Commerce (Sprint 30)
                .requestMatchers("/api/ecommerce/webhook/**").permitAll()
                .requestMatchers("/api/developer/**").hasAnyRole("MERCHANT", "OWNER")
                .requestMatchers("/api/integrations/**").hasAnyRole("MERCHANT", "OWNER")
                .requestMatchers("/api/v2/**").permitAll()
                // Live Tracking & Chat (Sprint 31)
                .requestMatchers("/api/tracking/sessions/shipment/**").authenticated()
                .requestMatchers("/api/tracking/**").hasAnyRole("COURIER", "OWNER", "ADMIN")
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/api/live-notifications/**").authenticated()
                // Payment Gateway & Settlements (Sprint 32)
                .requestMatchers("/api/payments/callback/**").permitAll()
                .requestMatchers("/api/payments/methods/**").authenticated()
                .requestMatchers("/api/payments/refunds/*/approve", "/api/payments/refunds/*/reject").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/payments/refunds").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("MERCHANT", "OWNER", "ADMIN")
                .requestMatchers("/api/settlements/**").hasAnyRole("OWNER", "ADMIN")
                // Security Hardening & Compliance (Sprint 33)
                .requestMatchers("/api/security/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/compliance/**").hasAnyRole("OWNER", "ADMIN")
                // Event-Driven Architecture & Async Jobs (Sprint 34)
                .requestMatchers("/api/events/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/jobs/**").hasAnyRole("OWNER", "ADMIN")
                // Multi-Tenant & White-Label Platform (Sprint 35)
                .requestMatchers("/api/tenants/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/invitations/*/accept").authenticated()
                .requestMatchers("/api/public/branding/**").permitAll()
                // User management endpoints
                .requestMatchers("/api/users/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/merchants/**").hasAnyRole("OWNER", "ADMIN")
                // All other API requests require authentication
                .requestMatchers("/api/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Configure authentication entry point to return 401 for unauthorized access
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityExceptionHandler)
            );

        return http.build();
    }

    // This bean defines the CORS rules
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // CORS origins loaded from application.yml (app.cors.allowed-origins)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Specify exact headers instead of "*"
        configuration.setAllowedHeaders(List.of(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials for authenticated requests
        configuration.setAllowCredentials(true);
        
        // Expose headers that the client needs
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "Content-Disposition"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}