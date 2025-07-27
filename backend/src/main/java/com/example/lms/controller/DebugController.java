package com.example.lms.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/debug", "/debug"})
public class DebugController {

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
}
