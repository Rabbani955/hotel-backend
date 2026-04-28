package com.marella.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔐 Strong secret key (minimum 32 chars for HS256)
    private static final String SECRET = "mySuperSecretKey12345678901234567890";

    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ⏱ Token validity (1 day)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // =====================================================
    // ✅ GENERATE TOKEN (WITH ROLE)
    // =====================================================
    public String generateToken(String username, String role) {

        // 🔥 SAFETY: always store role without "ROLE_"
        String cleanRole = (role != null && role.startsWith("ROLE_"))
                ? role.replace("ROLE_", "")
                : role;

        return Jwts.builder()
                .setSubject(username)
                .claim("role", cleanRole) // ✅ IMPORTANT
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================================================
    // ✅ EXTRACT USERNAME
    // =====================================================
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // =====================================================
    // ✅ EXTRACT ROLE
    // =====================================================
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    // =====================================================
    // ✅ VALIDATE TOKEN
    // =====================================================
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject() != null &&
                   !isTokenExpired(token);

        } catch (ExpiredJwtException e) {
            System.out.println("Token expired ❌");
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported token ❌");
        } catch (MalformedJwtException e) {
            System.out.println("Malformed token ❌");
        } catch (SignatureException e) {
            System.out.println("Invalid signature ❌");
        } catch (IllegalArgumentException e) {
            System.out.println("Token is empty ❌");
        }

        return false;
    }

    // =====================================================
    // ✅ CHECK EXPIRATION
    // =====================================================
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // =====================================================
    // ✅ EXTRACT ALL CLAIMS
    // =====================================================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}