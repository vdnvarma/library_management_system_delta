package com.example.lms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String jwtSecret;
    private final long jwtExpirationMs = 86400000; // 1 day
    
    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${jwt.secret}") String jwtSecret) {
        System.out.println("Using JWT secret from application properties");
        this.jwtSecret = jwtSecret;
        // Print first 3 characters of the secret followed by asterisks for security
        String maskedSecret = jwtSecret.length() > 3 
            ? jwtSecret.substring(0, 3) + "****"  
            : "****";
        System.out.println("JWT secret configured: " + maskedSecret);
    }

    public Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            System.out.println("JWT validation failed: " + e.getMessage());
            return false;
        }
    }
}
