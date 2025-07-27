package com.example.lms.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * This filter logs details about requests and responses, particularly useful
 * for debugging authentication and CORS issues.
 */
@Component
@Order(2) // Run after CORS filters but before security filters
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    
    // List of paths that should be excluded from body logging
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api/health", "/health",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request, 
            @org.springframework.lang.NonNull HttpServletResponse response, 
            @org.springframework.lang.NonNull FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Only wrap if not already wrapped - use final local variables 
        final HttpServletRequest requestToUse;
        final HttpServletResponse responseToUse;
        
        if (!(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        } else {
            requestToUse = request;
        }
        
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        } else {
            responseToUse = response;
        }
        
        long startTime = System.currentTimeMillis();
        String path = requestToUse.getRequestURI();
        String method = requestToUse.getMethod();
        
        // Check if we should log this request in detail
        boolean isDetailedLog = isDetailedLoggingPath(path);
        
        if (isDetailedLog) {
            System.out.println("\n========== REQUEST START ==========");
            System.out.println(method + " " + path);
            System.out.println("From: " + requestToUse.getRemoteAddr());
            System.out.println("User-Agent: " + requestToUse.getHeader("User-Agent"));
            System.out.println("Origin: " + requestToUse.getHeader("Origin"));
            System.out.println("Referer: " + requestToUse.getHeader("Referer"));
            
            // Log request headers
            System.out.println("\n----- Request Headers -----");
            Collections.list(requestToUse.getHeaderNames())
                .forEach(headerName -> {
                    System.out.println(headerName + ": " + requestToUse.getHeader(headerName));
                });
        }
        
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            if (isDetailedLog) {
                long duration = System.currentTimeMillis() - startTime;
                
                // Log the response
                System.out.println("\n----- Response -----");
                System.out.println("Status: " + responseToUse.getStatus());
                System.out.println("Time: " + duration + "ms");
                
                // Log response headers
                System.out.println("\n----- Response Headers -----");
                responseToUse.getHeaderNames().forEach(headerName -> {
                    System.out.println(headerName + ": " + responseToUse.getHeader(headerName));
                });
                
                // Log request body for certain paths
                if (!EXCLUDED_PATHS.contains(path) && !isMultipart(requestToUse) && !isBinaryContent(requestToUse)) {
                    String requestBody = getRequestBody(requestToUse);
                    if (requestBody != null && !requestBody.isEmpty()) {
                        System.out.println("\n----- Request Body -----");
                        System.out.println(requestBody);
                    }
                }
                
                // Log response body
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) responseToUse;
                String responseBody = getResponseBody(wrapper);
                if (responseBody != null && !responseBody.isEmpty() && !isBinaryResponse(responseToUse)) {
                    System.out.println("\n----- Response Body -----");
                    System.out.println(responseBody);
                }
                
                System.out.println("========== REQUEST END ==========\n");
                
                // Important: copy response body back for client
                wrapper.copyBodyToResponse();
            } else {
                // Just copy body to response without logging for non-detailed logs
                ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
            }
        }
    }
    
    private boolean isDetailedLoggingPath(String path) {
        // Detailed logging for authentication and CORS-related endpoints
        return path.contains("/login") || 
               path.contains("/register") || 
               path.contains("/api/users") || 
               path.contains("/cors-test") ||
               path.contains("/api/cors-test");
    }
    
    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
    
    private boolean isBinaryContent(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) return false;
        contentType = contentType.toLowerCase();
        return contentType.contains("image") || 
               contentType.contains("audio") || 
               contentType.contains("video") || 
               contentType.contains("application/octet-stream");
    }
    
    private boolean isBinaryResponse(HttpServletResponse response) {
        String contentType = response.getContentType();
        if (contentType == null) return false;
        contentType = contentType.toLowerCase();
        return contentType.contains("image") || 
               contentType.contains("audio") || 
               contentType.contains("video") || 
               contentType.contains("application/octet-stream");
    }
    
    private String getRequestBody(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length == 0) return "";
        
        try {
            return new String(buf, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            return "[Body cannot be decoded]";
        }
    }
    
    private String getResponseBody(ContentCachingResponseWrapper wrapper) {
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length == 0) return "";
        
        try {
            return new String(buf, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            return "[Body cannot be decoded]";
        }
    }
}
