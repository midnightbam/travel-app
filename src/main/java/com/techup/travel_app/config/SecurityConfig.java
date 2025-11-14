package com.techup.travel_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Disable CSRF for JWT-based API
            .csrf(csrf -> csrf.disable())
            
            // Configure session management for stateless JWT
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Allow unauthenticated access to auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow unauthenticated access to public API endpoints
                .requestMatchers("/api/trips/**", "/api/search/**").permitAll()
                
                // Allow access to static resources and frontend
                .requestMatchers("/", "/index.html", "/static/**", 
                                "/js/**", "/css/**", "/assets/**").permitAll()
                
                // Allow health check endpoints
                .requestMatchers("/actuator/health").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
