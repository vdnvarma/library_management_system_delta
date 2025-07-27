package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.Role;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/debug", "/debug"})
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "CORS is working correctly!");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Request received successfully");
        response.put("request", requestBody != null ? requestBody : "No body");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> listAllUsers() {
        List<Map<String, Object>> usersList = new ArrayList<>();
        
        userRepository.findAll().forEach(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("name", user.getName());
            userMap.put("role", user.getRole());
            // Don't include the password for security, but show if it exists
            userMap.put("hasPassword", user.getPassword() != null && !user.getPassword().isEmpty());
            userMap.put("passwordValue", user.getPassword()); // Only for debugging - remove in production
            
            usersList.add(userMap);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", usersList);
        response.put("count", usersList.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/headers")
    public ResponseEntity<?> showHeaders(@RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        
        // Log all headers for debugging (excluding sensitive ones)
        Map<String, String> safeHeaders = new HashMap<>();
        headers.forEach((key, value) -> {
            if (!key.toLowerCase().contains("authorization") && 
                !key.toLowerCase().contains("cookie")) {
                safeHeaders.put(key, value);
            } else {
                safeHeaders.put(key, "PRESENT BUT NOT SHOWN");
            }
        });
        
        response.put("headers", safeHeaders);
        return ResponseEntity.ok(response);
    }
}
