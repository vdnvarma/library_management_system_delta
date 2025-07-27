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

    @Value("${cors.debug:false}")
    private boolean corsDebug;
    
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        
        // Parse allowed origins from comma-separated string
        String[] origins = allowedOrigins.split(",");
        if (origins.length > 0 && !origins[0].equals("*")) {
            config.setAllowedOrigins(Arrays.asList(origins));
            if (corsDebug) {
                System.out.println("CORS - Setting specific allowed origins: " + Arrays.toString(origins));
            }
        } else {
            config.addAllowedOrigin("*");
            if (corsDebug) {
                System.out.println("CORS - Allowing all origins (*)");
            }
        }
        
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", 
                                             "X-Requested-With", "Access-Control-Request-Method", 
                                             "Access-Control-Request-Headers"));
        config.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", 
                                             "Authorization"));
        
        // When using specific origins, allowCredentials can be true
        // When using *, allowCredentials must be false
        config.setAllowCredentials(!allowedOrigins.contains("*"));
        
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        
        if (corsDebug) {
            System.out.println("CORS Configuration:");
            System.out.println("- Allowed origins: " + config.getAllowedOrigins());
            System.out.println("- Allow credentials: " + config.getAllowCredentials());
            System.out.println("- Allowed methods: " + config.getAllowedMethods());
            System.out.println("- Allowed headers: " + config.getAllowedHeaders());
        }
        
        return new CorsFilter(source);
    }
}
