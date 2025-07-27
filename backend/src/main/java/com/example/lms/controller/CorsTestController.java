package com.example.lms.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * A controller specifically for testing CORS functionality.
 */
@RestController
@RequestMapping("/api/cors-test")
public class CorsTestController {

    /**
     * Simple GET endpoint to test CORS configurations.
     * @param request The HTTP request
     * @return A map containing information about the request
     */
    @GetMapping
    public Map<String, String> testCors(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS test successful");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        response.put("origin", request.getHeader("Origin"));
        response.put("method", request.getMethod());
        
        System.out.println("CORS test endpoint called from origin: " + request.getHeader("Origin"));
        
        return response;
    }
    
    /**
     * POST endpoint to test CORS with a more complex request type.
     * @param request The HTTP request
     * @param body The request body
     * @return A map containing information about the request
     */
    @PostMapping
    public Map<String, Object> testCorsPost(
            HttpServletRequest request, 
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS POST test successful");
        response.put("timestamp", System.currentTimeMillis());
        response.put("origin", request.getHeader("Origin"));
        response.put("method", request.getMethod());
        
        if (body != null) {
            response.put("receivedData", body);
        }
        
        System.out.println("CORS POST test endpoint called from origin: " + request.getHeader("Origin"));
        
        return response;
    }
    
    /**
     * OPTIONS endpoint to explicitly handle preflight requests.
     * Spring should handle this automatically, but we provide it for diagnostic purposes.
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public Map<String, String> handleOptions(HttpServletRequest request) {
        System.out.println("CORS OPTIONS endpoint explicitly called from origin: " + request.getHeader("Origin"));
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS preflight successful");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return response;
    }
}
