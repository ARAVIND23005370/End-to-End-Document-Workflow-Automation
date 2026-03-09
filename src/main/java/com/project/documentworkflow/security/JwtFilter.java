package com.project.documentworkflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/h2-console/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("=== JWT FILTER EXECUTED ===");

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            System.out.println("Extracted Token: " + token);

            try {
                boolean isValid = jwtUtil.validateToken(token);
                System.out.println("Is Token Valid: " + isValid);

                if (isValid) {

                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRole(token);

                    System.out.println("Extracted Email: " + email);
                    System.out.println("Extracted Role: " + role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("Authentication Set Successfully");
                }

            } catch (Exception e) {
                System.out.println("Token Parsing Error: " + e.getMessage());
            }

        } else {
            System.out.println("No Bearer Token Found");
        }

        filterChain.doFilter(request, response);
    }
}