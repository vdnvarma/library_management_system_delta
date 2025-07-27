package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.Role;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/test", "/test"})
@CrossOrigin(origins = "*")  // Allow requests from any origin for test endpoints
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

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
    
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("pong");
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug/cors")
    public ResponseEntity<?> debugCors() {
        Map<String, Object> response = new HashMap<>();
        response.put("allowedOrigins", allowedOrigins);
        System.out.println("CORS Debug - Allowed Origins: " + allowedOrigins);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/loginWithDetails")
    public ResponseEntity<?> testLogin(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        boolean userFound = userOpt.isPresent();
        boolean passwordMatched = false;
        
        if (userFound) {
            User user = userOpt.get();
            passwordMatched = password.equals(user.getPassword());
        }
        
        response.put("success", passwordMatched);
        response.put("username", username);
        response.put("passwordSubmitted", password);
        response.put("userFound", userFound);
        response.put("passwordMatched", passwordMatched);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/createAdmin")
    public ResponseEntity<?> createAdminUser(
            @RequestParam(defaultValue = "admin") String username,
            @RequestParam(defaultValue = "admin123") String password,
            @RequestParam(defaultValue = "Administrator") String name) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            response.put("success", false);
            response.put("message", "User with username '" + username + "' already exists");
            return ResponseEntity.ok(response);
        }
        
        try {
            // Create new admin user
            User newAdmin = new User();
            newAdmin.setUsername(username);
            newAdmin.setPassword(password);
            newAdmin.setName(name);
            newAdmin.setRole(Role.ADMIN);
            
            User savedUser = userRepository.save(newAdmin);
            
            response.put("success", true);
            response.put("message", "Admin user created successfully");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "name", savedUser.getName(),
                "role", savedUser.getRole()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating admin user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/resetAdminPassword")
    public ResponseEntity<?> resetAdminPassword(
            @RequestParam(required = false, defaultValue = "admin") String username,
            @RequestParam(defaultValue = "admin123") String newPassword) {
        Optional<User> userToReset = userRepository.findByUsername(username);
        Map<String, Object> response = new HashMap<>();
        
        if (userToReset.isPresent()) {
            User user = userToReset.get();
            user.setPassword(newPassword);
            userRepository.save(user);
            
            response.put("success", true);
            response.put("message", "User '" + username + "' password reset to: " + newPassword);
            response.put("username", username);
        } else {
            response.put("success", false);
            response.put("message", "User '" + username + "' not found");
            response.put("username", username);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/checkAdminPassword")
    public ResponseEntity<?> checkAdminPassword() {
        Optional<User> adminUser = userRepository.findByUsername("admin");
        Map<String, Object> response = new HashMap<>();
        
        if (adminUser.isPresent()) {
            response.put("username", "admin");
            response.put("storedPassword", adminUser.get().getPassword());
            response.put("userExists", true);
        } else {
            response.put("userExists", false);
        }
        
        return ResponseEntity.ok(response);
    }
}
