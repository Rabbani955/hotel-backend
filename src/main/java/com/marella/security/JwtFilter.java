package com.marella.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

    	String uri = request.getRequestURI();
    	String authHeader = request.getHeader("Authorization");

    	// Skip JWT validation for public APIs
    	if (
    	        uri.equals("/api/bookings") ||
    	        uri.startsWith("/api/payment") ||
    	        uri.startsWith("/api/auth") ||
    	        uri.startsWith("/api/rooms") ||
    	        uri.equals("/api/bookings/occupied-rooms")
    	) {
    	    filterChain.doFilter(request, response);
    	    return;
    	}

    	try {

    	    System.out.println("=================================");
    	    System.out.println("REQUEST URI: " + uri);
    	    System.out.println("AUTH HEADER: " + authHeader);
    	    System.out.println("=================================");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                if (jwtUtil.isTokenValid(token)) {

                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);

                    System.out.println("TOKEN VALID ✅");
                    System.out.println("USERNAME: " + username);
                    System.out.println("ROLE FROM TOKEN: " + role);

                    // Convert role to Spring Security format
                    String finalRole = role;

                    if (finalRole == null || finalRole.isBlank()) {
                        finalRole = "ADMIN";
                    }

                    if (!finalRole.startsWith("ROLE_")) {
                        finalRole = "ROLE_" + finalRole.toUpperCase();
                    }

                    System.out.println("FINAL ROLE: " + finalRole);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    List.of(new SimpleGrantedAuthority(finalRole))
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    System.out.println("AUTHENTICATION SET SUCCESSFULLY ✅");
                } else {
                    System.out.println("TOKEN INVALID ❌");
                }
            } else {
                System.out.println("NO JWT TOKEN FOUND ❌");
            }

        } catch (Exception e) {

            System.out.println("JWT FILTER ERROR ❌");
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}