package com.example.lms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * Main application class for the Library Management System
 */
@SpringBootApplication
public class LmsWebApplication {
    
    @Value("${cors.allowed-origins:*}")
    private String corsAllowedOrigins;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    public static void main(String[] args) {
        SpringApplication.run(LmsWebApplication.class, args);
    }
    
    /**
     * Logs application startup information including URLs and configuration
     */
    @Bean
    public CommandLineRunner logStartupInfo(Environment env) {
        return args -> {
            System.out.println("\n------------------------------------------------");
            System.out.println("Library Management System Backend is starting up");
            System.out.println("------------------------------------------------");
            System.out.println("Active profiles: " + String.join(", ", env.getActiveProfiles()));
            System.out.println("CORS allowed origins: " + corsAllowedOrigins);
            System.out.println("Server port: " + serverPort);
            System.out.println("JVM version: " + System.getProperty("java.version"));
            System.out.println("OS: " + System.getProperty("os.name") + " " + 
                              System.getProperty("os.version"));
            System.out.println("------------------------------------------------\n");
        };
    }
    
    /**
     * Logs the server URL once it's fully started
     */
    @EventListener
    public void onServletWebServerInitialized(ServletWebServerInitializedEvent event) {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            int port = event.getWebServer().getPort();
            
            System.out.println("\n------------------------------------------------");
            System.out.println("LMS Backend started successfully!");
            System.out.println("Local access URL: http://localhost:" + port);
            System.out.println("External access URL: http://" + hostAddress + ":" + port);
            System.out.println("Access the API documentation at: http://localhost:" + port + "/api/health");
            System.out.println("------------------------------------------------\n");
        } catch (Exception e) {
            System.out.println("Could not determine server address: " + e.getMessage());
        }
    }
}