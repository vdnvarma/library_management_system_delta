package com.example.lms.config;

import com.example.lms.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2) // After the RequestLoggingFilter but before security filters
public class JwtDebugFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        
        // Skip static resources
        if (path.contains(".") || path.startsWith("/api/test/") || path.startsWith("/test/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check JWT token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                boolean valid = jwtUtil.validateJwtToken(token);
                System.out.println("JWT Token validation for path " + path + 
                                 " - Username: " + username +
                                 " - Valid: " + valid);
            } catch (Exception e) {
                System.out.println("JWT Token validation failed for path " + path + 
                                 " - Error: " + e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }
}
