package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.Role;
import com.example.lms.service.UserService;
import com.example.lms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users") // Path without /api prefix for compatibility
public class LegacyUserController {
    
    private final UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    public LegacyUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        System.out.println("Legacy login endpoint called for user: " + user.getUsername());
        
        Optional<User> found = userService.findByUsername(user.getUsername())
            .filter(u -> u.getPassword().equals(user.getPassword()));
        
        if (found.isPresent()) {
            String token = jwtUtil.generateToken(found.get().getUsername(), found.get().getRole().name());
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("id", found.get().getId());
            responseMap.put("name", found.get().getName());
            responseMap.put("username", found.get().getUsername());
            responseMap.put("role", found.get().getRole());
            responseMap.put("token", token);
            
            return ResponseEntity.ok(responseMap);
        } else {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Invalid credentials");
            return ResponseEntity.status(401).body(errorMap);
        }
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        System.out.println("Legacy register endpoint called for user: " + user.getUsername());
        if (user.getRole() == null) user.setRole(Role.STUDENT);
        return userService.save(user);
    }
}
