package com.example.lms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Log request details
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        System.out.println("Request path: " + path + " with HTTP method: " + method);
        
        // Log authorization header (useful for debugging JWT issues)
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null) {
            System.out.println("Authorization header found for path: " + path);
        } else {
            System.out.println("No Authorization header found for path: " + path);
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
        
        // Log response status (after processing)
        int status = httpResponse.getStatus();
        if (status >= 400) {
            System.out.println("Request to " + path + " resulted in error status: " + status);
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
