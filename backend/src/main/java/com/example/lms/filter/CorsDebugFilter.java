package com.example.lms.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This filter logs CORS-related headers for debugging purposes.
 */
@Component
@Order(1)
public class CorsDebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String origin = httpRequest.getHeader("Origin");
        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();
        
        // Only log if there's an Origin header (CORS request)
        if (origin != null) {
            System.out.println("====== CORS REQUEST ======");
            System.out.println("Path: " + path);
            System.out.println("Method: " + method);
            System.out.println("Origin: " + origin);
            System.out.println("Access-Control-Request-Method: " + httpRequest.getHeader("Access-Control-Request-Method"));
            System.out.println("Access-Control-Request-Headers: " + httpRequest.getHeader("Access-Control-Request-Headers"));
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
        
        // Log response headers for CORS requests
        if (origin != null && response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            System.out.println("====== CORS RESPONSE ======");
            System.out.println("Status: " + httpResponse.getStatus());
            System.out.println("Access-Control-Allow-Origin: " + httpResponse.getHeader("Access-Control-Allow-Origin"));
            System.out.println("Access-Control-Allow-Methods: " + httpResponse.getHeader("Access-Control-Allow-Methods"));
            System.out.println("Access-Control-Allow-Headers: " + httpResponse.getHeader("Access-Control-Allow-Headers"));
            System.out.println("Access-Control-Allow-Credentials: " + httpResponse.getHeader("Access-Control-Allow-Credentials"));
        }
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
