package com.twsela.security;

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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(AuthenticationProvider authenticationProvider, 
                         JwtAuthenticationFilter jwtAuthFilter,
                         SecurityExceptionHandler securityExceptionHandler) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
        this.securityExceptionHandler = securityExceptionHandler;
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
                .requestMatchers("/api/auth/login", "/api/v1/auth/**", "/api/public/**", "/api/debug/**").permitAll()
                // Swagger/OpenAPI documentation endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**", "/webjars/**").permitAll()
                // Static files - no authentication required
                .requestMatchers("/", "/index.html", "/login.html", "/contact.html", "/profile.html", "/settings.html", "/404.html").permitAll()
                .requestMatchers("/frontend/**", "/static/**", "/*.html", "/*/*.html", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.ico", "/*.svg", "/src/**").permitAll()
                .requestMatchers("/merchant/**", "/owner/**", "/courier/**", "/warehouse/**", "/admin/**").permitAll()
                // Actuator endpoints for monitoring
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics", "/actuator/prometheus").permitAll()
                // Authentication required for /api/auth/me and /api/v1/auth/me
                .requestMatchers("/api/auth/me", "/api/v1/auth/me").authenticated()
                // All other auth endpoints are open for registration, password reset, etc.
                .requestMatchers("/api/auth/**", "/api/v1/auth/**").permitAll()
                // Unified endpoints with role-based access control
                .requestMatchers("/api/shipments/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER")
                .requestMatchers("/api/master/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/dashboard/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/manifests/**").hasAnyRole("OWNER", "ADMIN", "COURIER")
                .requestMatchers("/api/financial/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER")
                .requestMatchers("/api/reports/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/warehouse/**").hasAnyRole("OWNER", "ADMIN", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/settings/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/contact/**").permitAll()
                .requestMatchers("/api/statistics/**").hasAnyRole("OWNER", "ADMIN", "MERCHANT", "COURIER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/audit/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/sms/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/backup/**").hasAnyRole("OWNER", "ADMIN")
                // User management endpoints
                .requestMatchers("/api/users/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/merchants/**").hasAnyRole("OWNER", "ADMIN")
                // All other API requests require authentication
                .requestMatchers("/api/**").authenticated()
                // Static resources are permitted
                .anyRequest().permitAll()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
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
        
        // In production, specify exact domains instead of "*"
        // For development, allow localhost and common development ports
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Vite dev server
            "http://localhost:5174",  // Vite dev server (fallback port)
            "http://127.0.0.1:5173", // Vite dev server (127.0.0.1)
            "http://127.0.0.1:5174", // Vite dev server (127.0.0.1 fallback)
            "http://localhost:8000",  // Frontend development server (HTTP)
            "https://localhost:8000", // Frontend development server (HTTPS)
            "http://127.0.0.1:8000",  // Frontend development server (127.0.0.1)
            "https://127.0.0.1:8000", // Frontend development server (127.0.0.1 HTTPS)
            "http://localhost:8080",  // Backend API server (HTTP)
            "https://localhost:8080"  // Backend API server (HTTPS)
        ));
        
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