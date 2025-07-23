package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.Role;
import com.example.lms.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.List;
import com.example.lms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
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
            return ResponseEntity.ok(Map.of(
                "id", found.get().getId(),
                "name", found.get().getName(),
                "username", found.get().getUsername(),
                "role", found.get().getRole(),
                "token", token
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
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
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can view all users"));
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
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
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
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can change user roles"));
        }
        
        user.setName(userDetails.getName());
        // Only update username if provided and not already taken
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(user.getUsername())) {
            if (userService.findByUsername(userDetails.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
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
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can delete users"));
        }
        
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleRequest) {
        // Only admins can change roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can change user roles"));
        }
        
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            String roleName = roleRequest.get("role");
            if (roleName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
            }
            
            Role role = Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
            return ResponseEntity.ok(userService.save(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }
    }
    
    @GetMapping("/byRole/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        // Only admins can list users by role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can list users by role"));
        }
        
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            List<User> users = userService.findByRole(roleEnum);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }
    }
} 