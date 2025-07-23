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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
    
    // CORS configuration is now handled by WebConfig.webCorsConfigurer

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/search").permitAll()
                
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
            
            // For debugging purposes - print the path being accessed
            String path = request.getServletPath();
            System.out.println("Request path: " + path);
            
            // CORS headers are now handled by Spring Security's CORS configuration
            
            // For preflight requests - let Spring handle OPTIONS requests
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
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
                }
            } else {
                System.out.println("No Authorization header found or not Bearer token");
            }
            filterChain.doFilter(request, response);
        }
    }
}
