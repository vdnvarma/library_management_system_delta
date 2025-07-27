package com.example.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Value("${cors.allowed-origins:https://lmsbeta.onrender.com,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer webCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@org.springframework.lang.NonNull CorsRegistry registry) {
                // Allow all origins temporarily for debugging
                registry.addMapping("/**")
                        .allowedOrigins("*") // Allow all origins temporarily for debugging
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(false) // Must be false when using allowedOrigins("*")
                        .maxAge(3600); // 1 hour
            }
        };
    }
}
