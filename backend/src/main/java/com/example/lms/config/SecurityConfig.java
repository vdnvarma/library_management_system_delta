package com.example.lms.config;

import com.example.lms.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private CorsFilter corsFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Add CORS filter first
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            // Configure CORS
            .cors(cors -> {})
            // Disable CSRF for API
            .csrf(csrf -> csrf.disable())
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (both with and without /api prefix)
                .requestMatchers(
                    "/api/users/login", "/users/login",
                    "/api/users/register", "/users/register",
                    "/api/health", "/health",
                    "/api/debug/**", "/debug/**",
                    "/api/test/**", "/test/**"
                ).permitAll()
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, 
                    "/api/books", "/books",
                    "/api/books/search", "/books/search"
                ).permitAll()
                
                // Student endpoints (most specific paths first)
                .requestMatchers(HttpMethod.POST, "/api/issues/issue").hasAnyRole("ADMIN", "LIBRARIAN", "STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/issues/user/**").hasAnyRole("ADMIN", "LIBRARIAN", "STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/reservations/user/**").hasAnyRole("ADMIN", "LIBRARIAN", "STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/reservations/reserve").hasAnyRole("ADMIN", "LIBRARIAN", "STUDENT")
                
                // Admin only endpoints
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Librarian endpoints (after more specific paths)
                .requestMatchers(HttpMethod.POST, "/api/books").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers("/api/issues/**").hasAnyRole("ADMIN", "LIBRARIAN")
                .requestMatchers("/api/reservations/**").hasAnyRole("ADMIN", "LIBRARIAN")
                
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    public static class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtUtil jwtUtil;

        public JwtAuthFilter(JwtUtil jwtUtil) {
            this.jwtUtil = jwtUtil;
        }

        @Override
        protected void doFilterInternal(
                @org.springframework.lang.NonNull HttpServletRequest request,
                @org.springframework.lang.NonNull HttpServletResponse response,
                @org.springframework.lang.NonNull FilterChain filterChain)
                throws ServletException, IOException {
            String header = request.getHeader("Authorization");
            
            // For debugging purposes - print detailed request info
            String path = request.getServletPath();
            String method = request.getMethod();
            String origin = request.getHeader("Origin");
            System.out.println("Request: " + method + " " + path + " from origin: " + origin);
            System.out.println("Auth header: " + (header != null ? "present" : "absent"));
            
            // Always add CORS headers for better browser compatibility
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
            response.setHeader("Access-Control-Max-Age", "3600");
            
            // For preflight requests - let Spring handle OPTIONS requests
            if ("OPTIONS".equalsIgnoreCase(method)) {
                response.setStatus(HttpServletResponse.SC_OK);
                filterChain.doFilter(request, response);
                return;
            }
            
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    if (jwtUtil.validateJwtToken(token)) {
                        String username = jwtUtil.getUsernameFromToken(token);
                        // Extract role claim from JWT
                        String role = Jwts.parserBuilder()
                            .setSigningKey(jwtUtil.getSigningKey())
                            .build()
                            .parseClaimsJws(token)
                            .getBody()
                            .get("role", String.class);
                        
                        System.out.println("Authenticated user: " + username + " with role: " + role);
                        
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                        
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception e) {
                    System.out.println("JWT validation error: " + e.getMessage());
                    // Add more detailed error logging
                    if (token != null && token.length() > 10) {
                        System.out.println("Token starts with: " + token.substring(0, 10) + "...");
                    }
                }
            } else {
                // Check if this is a public endpoint that doesn't need authentication
                boolean isPublicEndpoint = 
                    path.equals("/api/health") || path.equals("/health") ||
                    path.equals("/api/users/login") || path.equals("/users/login") ||
                    path.equals("/api/users/register") || path.equals("/users/register") ||
                    path.startsWith("/api/books/search") || path.startsWith("/books/search") ||
                    path.equals("/api/books") || path.equals("/books") ||
                    path.startsWith("/api/debug") || path.startsWith("/debug");
                
                if (!isPublicEndpoint) {
                    System.out.println("No Authorization header found for protected path: " + path);
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
