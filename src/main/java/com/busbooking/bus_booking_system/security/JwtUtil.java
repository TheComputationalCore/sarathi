package com.busbooking.bus_booking_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 characters."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /* ============================
       CLAIM EXTRACTION
    ============================ */

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /* ============================
       TOKEN GENERATION
    ============================ */

    public String generateToken(UserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("roles",
                userDetails.getAuthorities()
                        .stream()
                        .map(auth -> auth.getAuthority())
                        .toList()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(signingKey)
                .compact();
    }

    /* ============================
       VALIDATION
    ============================ */

    // Used in WebSocketConfig (secure validation)
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Used in HTTP filters (basic validation)
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}