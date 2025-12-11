package com.twsela.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collection;
import java.util.List;
import org.springframework.lang.NonNull;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String username;

        // Skip JWT processing for login endpoint and other public auth endpoints
        String requestUri = request.getRequestURI();
        if (requestUri.equals("/api/auth/login") || 
            requestUri.equals("/api/auth/register") ||
            requestUri.equals("/api/auth/forgot-password") ||
            requestUri.equals("/api/auth/reset-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Invalid JWT token - let Spring Security handle it
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtService.isTokenValid(jwt, username)) { // استخدام username بدلاً من userDetails.getUsername()
                    
                    // Extract role from JWT token
                    String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                    
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                    
                    // If role is found in JWT, use it instead of userDetails authorities
                    if (role != null) {
                        authorities = List.of(new SimpleGrantedAuthority(role));
                    } else {
                    }
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
                // User not found or other error - let Spring Security handle it
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}



