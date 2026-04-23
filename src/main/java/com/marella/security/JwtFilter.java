package com.marella.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ 1. SKIP AUTH APIs (LOGIN, REGISTER, VALIDATE)
        if (path.startsWith("/api/auth") ||
        	    path.startsWith("/api/bookings") ||
        	    path.startsWith("/api/rooms") ||
        	    path.startsWith("/api/payment")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");

            // ✅ 2. CHECK TOKEN EXISTS
            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                // ✅ 3. VALIDATE TOKEN
                if (jwtUtil.isTokenValid(token)) {

                    String role = jwtUtil.extractRole(token);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    "admin",
                                    null,
                                    Collections.singleton(() -> "ROLE_" + role)
                            );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        } catch (Exception e) {
            // ❗ DO NOT BLOCK REQUEST — just log
            System.out.println("JWT Error: " + e.getMessage());
        }

        // ✅ 4. CONTINUE FILTER CHAIN ALWAYS
        filterChain.doFilter(request, response);
    }
}