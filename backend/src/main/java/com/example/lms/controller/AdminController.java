package com.example.lms.controller;

import com.example.lms.model.Role;
import com.example.lms.model.User;
import com.example.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        // Only admin can update roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can update user roles"));
        }
        
        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        String roleStr = payload.get("role");
        if (roleStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role not specified"));
        }
        
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            user.setRole(role);
            userService.save(user);
            return ResponseEntity.ok(Map.of("message", "Role updated successfully", "user", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + roleStr));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getSystemStatistics() {
        // Only admin can view system statistics
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "Only administrators can view system statistics"));
        }
        
        Map<String, Object> statistics = Map.of(
            "totalUsers", userService.getAllUsers().size(),
            "studentCount", userService.getAllUsers().stream().filter(u -> u.getRole() == Role.STUDENT).count(),
            "librarianCount", userService.getAllUsers().stream().filter(u -> u.getRole() == Role.LIBRARIAN).count(),
            "adminCount", userService.getAllUsers().stream().filter(u -> u.getRole() == Role.ADMIN).count()
        );
        
        return ResponseEntity.ok(statistics);
    }
}
