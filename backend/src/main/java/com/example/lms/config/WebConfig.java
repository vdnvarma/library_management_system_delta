package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer webCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
                if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
                    allowedOrigins = "http://localhost:3000,https://*.onrender.com";
                }
                System.out.println("CORS configured with allowed origins: " + allowedOrigins);
                
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
