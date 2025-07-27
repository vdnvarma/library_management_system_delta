package com.example.lms.controller;

import com.example.lms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * This controller provides endpoints for testing JWT functionality
 */
@RestController
@RequestMapping("/api/test/jwt")
public class JwtTestController {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtTestController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Tests if JWT token generation is working properly
     */
    @GetMapping("/test")
    public ResponseEntity<?> testJwt() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Try to generate a test token
            String testToken = jwtUtil.generateToken("test-user", "ADMIN");
            
            // Check if token is valid
            boolean isValid = jwtUtil.validateJwtToken(testToken);
            
            response.put("status", "success");
            response.put("message", "JWT generation is working properly");
            response.put("tokenValid", isValid);
            response.put("tokenLength", testToken.length());
            
            // Get the username from the token for verification
            String username = jwtUtil.getUsernameFromToken(testToken);
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "JWT generation failed: " + e.getMessage());
            response.put("exceptionType", e.getClass().getName());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
