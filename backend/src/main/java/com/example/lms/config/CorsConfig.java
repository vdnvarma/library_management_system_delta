package com.example.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.debug:true}")
    private boolean corsDebug;
    
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        
        // For debugging, always output CORS configuration
        System.out.println("CORS Configuration initializing");
        System.out.println("Raw allowed origins: " + allowedOrigins);
        
        // Handle allowed origins
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty() || "*".equals(allowedOrigins.trim())) {
            // Wildcard case
            config.addAllowedOrigin("*");
            config.setAllowCredentials(false); // Must be false with wildcard origin
            System.out.println("CORS: Using wildcard origin with allowCredentials=false");
        } else {
            // Specific origins case
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                String trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    config.addAllowedOrigin(trimmedOrigin);
                    System.out.println("CORS: Added allowed origin: " + trimmedOrigin);
                }
            }
            config.setAllowCredentials(true);
            System.out.println("CORS: Using specific origins with allowCredentials=true");
        }
        
        // Set allowed methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // Set allowed headers
        config.setAllowedHeaders(Collections.singletonList("*"));
        
        // Expose headers
        config.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Credentials", 
            "Authorization"
        ));
        
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        
        if (corsDebug) {
            System.out.println("CORS Configuration summary:");
            System.out.println("- Allowed origins: " + config.getAllowedOrigins());
            System.out.println("- Allow credentials: " + config.getAllowCredentials());
            System.out.println("- Allowed methods: " + config.getAllowedMethods());
            System.out.println("- Allowed headers: " + config.getAllowedHeaders());
        }
        
        return new CorsFilter(source);
    }
}
