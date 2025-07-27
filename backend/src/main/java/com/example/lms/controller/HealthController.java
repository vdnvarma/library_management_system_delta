package com.example.lms.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.datasource.url:not-set}")
    private String dbUrl;
    
    @Value("${spring.datasource.username:not-set}")
    private String dbUser;
    
    @GetMapping
    public ResponseEntity<?> checkHealth() {
        // Mask sensitive values for security
        String maskedDbUrl = dbUrl.replaceAll(":[^:]*@", ":***@");
        String maskedDbUser = dbUser.substring(0, Math.min(dbUser.length(), 3)) + "***";
        
        return ResponseEntity.ok(Map.of(
            "status", "up",
            "database", Map.of(
                "url", maskedDbUrl,
                "user", maskedDbUser
            ),
            "env", Map.of(
                "jwtConfigured", System.getenv("JWT_SECRET") != null
            )
        ));
    }
}
