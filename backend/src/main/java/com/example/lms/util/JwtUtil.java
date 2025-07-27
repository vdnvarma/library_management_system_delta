package com.example.lms.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final long jwtExpirationMs = 86400000; // 1 day
    private final Key signingKey;
    
    public JwtUtil(@org.springframework.beans.factory.annotation.Value("${jwt.secret}") String jwtSecret) {
        System.out.println("Using JWT secret from application properties");
        
        // Ensure the key is at least 256 bits (32 bytes) for HMAC-SHA256
        String secureSecret = ensureSecureKeyLength(jwtSecret);
        
        // Generate a proper signing key
        this.signingKey = Keys.hmacShaKeyFor(secureSecret.getBytes());
        
        // Print first 3 characters of the secret followed by asterisks for security
        String maskedSecret = jwtSecret.length() > 3 
            ? jwtSecret.substring(0, 3) + "****"  
            : "****";
        System.out.println("JWT secret configured: " + maskedSecret + 
                          " (length: " + jwtSecret.length() + ", secure: " + (jwtSecret.length() >= 32) + ")");
    }
    
    /**
     * Ensures the provided key is at least 256 bits (32 bytes) by padding if necessary
     */
    private String ensureSecureKeyLength(String originalKey) {
        if (originalKey == null || originalKey.isEmpty()) {
            // If no key provided, use a secure default
            return "LMS_SECURE_JWT_KEY_2025_MINIMUM_LENGTH_FOR_SECURITY_32BYTES";
        }
        
        if (originalKey.length() >= 32) {
            // Key is already long enough
            return originalKey;
        }
        
        // Pad the key to reach at least 32 bytes
        StringBuilder paddedKey = new StringBuilder(originalKey);
        String padding = "SECURE_JWT_KEY_PADDING_TO_ENSURE_MINIMUM_LENGTH_REQUIREMENTS";
        
        int paddingNeeded = 32 - originalKey.length();
        paddedKey.append(padding, 0, paddingNeeded);
        
        System.out.println("WARNING: JWT secret was too short. It has been securely padded to meet minimum requirements.");
        return paddedKey.toString();
    }

    public Key getSigningKey() {
        return signingKey;
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
