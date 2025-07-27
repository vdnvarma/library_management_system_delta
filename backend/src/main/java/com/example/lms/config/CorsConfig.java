package com.example.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

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
        
        // First, identify production and local development origins
        boolean hasWildcard = false;
        boolean hasProductionOrigin = false;
        boolean hasLocalOrigin = false;
        
        // Start with explicit high priority origins
        // =================== IMPORTANT: Production Frontend ===================
        config.addAllowedOrigin("https://lmsbeta.onrender.com");
        
        // For development
        config.addAllowedOrigin("http://localhost:3000");
        
        // For testing across different backend instances
        config.addAllowedOrigin("https://library-management-system-backend-lms-demo.onrender.com");
        config.addAllowedOrigin("https://library-management-system-backend-jlb9.onrender.com");
        
        System.out.println("CORS: Added critical origins directly");
        hasProductionOrigin = true;
        hasLocalOrigin = true;
        
        // Also process origins from properties
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty() || "*".equals(allowedOrigins.trim())) {
            // Wildcard case - note that we can't use this with credentials
            System.out.println("CORS: Wildcard pattern detected in configuration");
            hasWildcard = true;
        } else {
            // Specific origins case
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                String trimmedOrigin = origin.trim();
                if (!trimmedOrigin.isEmpty()) {
                    // Don't add duplicates
                    if (!"https://lmsbeta.onrender.com".equals(trimmedOrigin) && 
                        !"http://localhost:3000".equals(trimmedOrigin)) {
                        config.addAllowedOrigin(trimmedOrigin);
                        System.out.println("CORS: Added additional allowed origin: " + trimmedOrigin);
                    }
                    
                    if (trimmedOrigin.contains("render.com")) {
                        hasProductionOrigin = true;
                    }
                    if (trimmedOrigin.contains("localhost")) {
                        hasLocalOrigin = true;
                    }
                }
            }
        }
        
        // Set allowCredentials based on our origin configuration
        // We can only use credentials with specific origins, not with wildcard
        if (hasWildcard && !hasProductionOrigin && !hasLocalOrigin) {
            // Only wildcard - can't use credentials
            config.addAllowedOrigin("*");
            config.setAllowCredentials(false);
            System.out.println("CORS: Using wildcard origin with allowCredentials=false");
        } else {
            // Specific origins - can use credentials
            config.setAllowCredentials(true);
            System.out.println("CORS: Using specific origins with allowCredentials=true");
        }
        
        // Set allowed methods (make sure OPTIONS is included)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // Set allowed headers (expand the list for broader compatibility)
        config.setAllowedHeaders(Arrays.asList(
            "Origin", 
            "Content-Type", 
            "Accept", 
            "Authorization", 
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Access-Control-Allow-Origin",
            "Cache-Control",
            "Pragma"
        ));
        
        // Expose headers
        config.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", 
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Credentials", 
            "Access-Control-Max-Age",
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
