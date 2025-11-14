package com.techup.travel_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins for development and production
        configuration.addAllowedOrigin("http://localhost:5173"); // Vite dev server
        configuration.addAllowedOrigin("http://localhost:3000");  // Alternative dev port
        configuration.addAllowedOrigin("http://localhost:8080");  // Backend serving static
        
        // Allow all HTTP methods
        configuration.addAllowedMethod("*");
        
        // Allow all headers
        configuration.addAllowedHeader("*");
        
        // Allow credentials (important for JWT tokens)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
