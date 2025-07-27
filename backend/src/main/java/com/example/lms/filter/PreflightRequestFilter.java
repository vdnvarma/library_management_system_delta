package com.example.lms.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This filter ensures preflight requests are properly handled even before
 * Spring Security can process them. It's especially useful for handling CORS
 * preflight requests for login endpoints.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PreflightRequestFilter implements Filter {
    
    @Value("${cors.allowed-origins:*}")
    private String allowedOriginsConfig;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        // Only handle OPTIONS method and requests with an Origin header
        String method = request.getMethod();
        String origin = request.getHeader("Origin");
        String path = request.getRequestURI();
        
        if ("OPTIONS".equalsIgnoreCase(method) && origin != null) {
            System.out.println("============ PREFLIGHT REQUEST INTERCEPTED ============");
            System.out.println("Path: " + path);
            System.out.println("Origin: " + origin);
            System.out.println("Access-Control-Request-Method: " + request.getHeader("Access-Control-Request-Method"));
            System.out.println("Access-Control-Request-Headers: " + request.getHeader("Access-Control-Request-Headers"));
            
            // Critical path login endpoint
            if (path.contains("/api/users/login") || path.contains("/users/login") ||
                path.contains("/api/users/register") || path.contains("/users/register")) {
                
                System.out.println("CRITICAL PATH: Authentication endpoint detected");
                
                // Check if the origin is allowed
                boolean originAllowed = "*".equals(allowedOriginsConfig) || 
                                       allowedOriginsConfig.contains(origin);
                
                if (originAllowed) {
                    // Set CORS headers directly
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Methods", 
                                       "GET, POST, PUT, DELETE, OPTIONS");
                    response.setHeader("Access-Control-Allow-Headers", 
                                      "Authorization, Content-Type, X-Requested-With, Origin");
                    response.setHeader("Access-Control-Max-Age", "3600");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    
                    // Return 200 OK status
                    response.setStatus(HttpServletResponse.SC_OK);
                    
                    System.out.println("PREFLIGHT REQUEST APPROVED for " + origin);
                    System.out.println("============ END PREFLIGHT ============");
                    
                    // End the request here
                    return;
                } else {
                    System.out.println("Origin not allowed: " + origin);
                }
            }
            
            System.out.println("============ END PREFLIGHT (continuing chain) ============");
        }
        
        // Continue with the filter chain for non-preflight requests
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to initialize
    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }
}
