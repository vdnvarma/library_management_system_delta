package com.example.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration that also adds CORS settings at the Spring MVC level.
 * This complements the CorsFilter configuration.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(@org.springframework.lang.NonNull CorsRegistry registry) {
        System.out.println("Setting up CORS in WebMvcConfig with allowed origins: " + allowedOrigins);
        
        registry.addMapping("/**")
            .allowedOrigins("https://lmsbeta.onrender.com", "http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization", 
                           "X-Requested-With", "Access-Control-Request-Method", 
                           "Access-Control-Request-Headers", "Access-Control-Allow-Origin")
            .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Methods",
                          "Access-Control-Allow-Headers", "Access-Control-Allow-Credentials", "Authorization")
            .allowCredentials(true)
            .maxAge(3600);
            
        System.out.println("CORS mappings configured in WebMvcConfig");
    }
}
