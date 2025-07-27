package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.Role;
import com.example.lms.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.example.lms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users") // Standard path with /api prefix
public class UserController {
    private final UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        if (user.getRole() == null) user.setRole(Role.STUDENT);
        return userService.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
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
    
    // Admin-only endpoints
    
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // Check if user is admin 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Only administrators can view all users");
            return ResponseEntity.status(403).body(errorMap);
        }
        
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        // Check if user is admin or requesting their own info
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Allow access if admin or user accessing own record
        if (!isAdmin && (currentUser == null || !currentUser.getId().equals(id))) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Access denied");
            return ResponseEntity.status(403).body(errorMap);
        }
        
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        // Check if user is admin or updating their own info
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername).orElse(null);
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Allow access if admin or user updating own record
        if (!isAdmin && (currentUser == null || !currentUser.getId().equals(id))) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        
        // Non-admins cannot change their role
        if (!isAdmin && userDetails.getRole() != user.getRole()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Only administrators can change user roles");
            return ResponseEntity.status(403).body(errorMap);
        }
        
        user.setName(userDetails.getName());
        // Only update username if provided and not already taken
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(user.getUsername())) {
            if (userService.findByUsername(userDetails.getUsername()).isPresent()) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Username already taken");
                return ResponseEntity.badRequest().body(errorMap);
            }
            user.setUsername(userDetails.getUsername());
        }
        
        // Update password if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }
        
        // Only admin can update role
        if (isAdmin && userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        return ResponseEntity.ok(userService.save(user));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // Only admins can delete users
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Only administrators can delete users");
            return ResponseEntity.status(403).body(errorMap);
        }
        
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteUser(id);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "User deleted successfully");
        return ResponseEntity.ok(responseMap);
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleRequest) {
        // Only admins can change roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Only administrators can change user roles");
            return ResponseEntity.status(403).body(errorMap);
        }
        
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            String roleName = roleRequest.get("role");
            if (roleName == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "Role is required");
                return ResponseEntity.badRequest().body(errorMap);
            }
            
            Role role = Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
            return ResponseEntity.ok(userService.save(user));
        } catch (IllegalArgumentException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Invalid role");
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    @GetMapping("/byRole/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        // Only admins can list users by role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Only administrators can list users by role");
            return ResponseEntity.status(403).body(errorMap);
        }
        
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            List<User> users = userService.findByRole(roleEnum);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Invalid role");
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
} 