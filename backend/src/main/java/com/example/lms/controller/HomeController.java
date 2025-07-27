package com.example.lms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller handles the root path to avoid 403 errors
 */
@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    @ResponseBody
    public String home() {
        return "Library Management System API - Backend is running.";
    }
    
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "UP";
    }
}
