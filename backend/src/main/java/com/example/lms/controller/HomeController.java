package com.example.lms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This controller handles the root path and health checks
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    @ResponseBody
    public Map<String, Object> home(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "up");
        response.put("message", "Library Management System API is running");
        response.put("origin", request.getHeader("Origin"));
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "up");
        response.put("service", "LMS API");
        response.put("origin", request.getHeader("Origin"));
        response.put("timestamp", System.currentTimeMillis());
        
        // Add some system information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB");
        systemInfo.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB");
        systemInfo.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB");
        
        response.put("system", systemInfo);
        return response;
    }
    
    // Remove duplicate /api/health mapping as it's handled by HealthController
}
