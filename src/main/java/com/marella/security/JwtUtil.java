package com.marella.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔐 Use strong secret (at least 256-bit)
    private static final String SECRET = "mySuperSecretKey12345678901234567890";
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION_TIME = 86400000; // 1 day

    // ✅ GENERATE TOKEN (WITH ROLE)
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // dynamic role
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ EXTRACT USERNAME
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ EXTRACT ROLE
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ✅ CHECK TOKEN EXPIRATION
    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // ✅ VALIDATE TOKEN (FULL CHECK)
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            // check expiry
            return !claims.getExpiration().before(new Date());

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

    // ✅ EXTRACT ALL CLAIMS
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}