package com.example.lms.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/test", "/test"})
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Public endpoint is accessible");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login-test")
    public ResponseEntity<?> loginTest(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "received");
        response.put("username", credentials.get("username"));
        response.put("passwordLength", credentials.get("password") != null ? credentials.get("password").length() : 0);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
