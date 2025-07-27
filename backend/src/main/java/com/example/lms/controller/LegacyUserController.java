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
        
        Optional<User> userFound = userService.findByUsername(user.getUsername());
        boolean userExists = userFound.isPresent();
        System.out.println("User found in database: " + userExists);
        
        boolean passwordMatch = false;
        if (userExists) {
            passwordMatch = userFound.get().getPassword().equals(user.getPassword());
            System.out.println("Password match: " + passwordMatch);
            System.out.println("Input password: " + user.getPassword() + ", Stored password: " + userFound.get().getPassword());
        }
        
        if (userExists && passwordMatch) {
            String token = jwtUtil.generateToken(userFound.get().getUsername(), userFound.get().getRole().name());
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("id", userFound.get().getId());
            responseMap.put("name", userFound.get().getName());
            responseMap.put("username", userFound.get().getUsername());
            responseMap.put("role", userFound.get().getRole());
            responseMap.put("token", token);
            
            System.out.println("Login successful for user: " + user.getUsername());
            return ResponseEntity.ok(responseMap);
        } else {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Invalid credentials");
            System.out.println("Login failed for user: " + user.getUsername() + 
                              " - User exists: " + userExists + 
                              " - Password match: " + passwordMatch);
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
