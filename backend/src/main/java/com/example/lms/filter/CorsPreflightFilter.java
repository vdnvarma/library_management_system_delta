package com.example.lms.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * This filter adds CORS headers to OPTIONS preflight requests.
 * It runs before any security filters to ensure preflight requests succeed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsPreflightFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Handle OPTIONS requests (preflight)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            String origin = httpRequest.getHeader("Origin");
            System.out.println("CORS Preflight request from origin: " + origin);
            
            // Add CORS headers directly for OPTIONS requests
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            httpResponse.setHeader("Access-Control-Allow-Headers", 
                "Origin, Content-Type, Accept, Authorization, X-Requested-With, Access-Control-Request-Method, Access-Control-Request-Headers");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            
            // Return 200 OK for preflight
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            System.out.println("CORS Preflight response sent with allowed origin: " + origin);
            return;
        }
        
        // Continue with other requests
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
