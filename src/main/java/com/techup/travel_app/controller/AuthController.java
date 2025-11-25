package com.techup.travel_app.controller;

import com.techup.travel_app.entity.User;
import com.techup.travel_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        // For demo purposes, also accept username field as email
        if (email == null) {
            email = credentials.get("username");
        }
        
        if (email == null || password == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Email and password are required");
            return ResponseEntity.status(400).body(error);
        }
        
        // Try to find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            // For demo: allow demo@example.com with password "password"
            if ("demo@example.com".equals(email) && "password".equals(password)) {
                Map<String, Object> response = new HashMap<>();
                response.put("token", "demo-jwt-token-1-" + System.currentTimeMillis());
                response.put("user", Map.of(
                    "id", 1L,
                    "email", email,
                    "displayName", "Demo User",
                    "role", "USER"
                ));
                response.put("message", "Login successful");
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
            
            // User not found - return generic error for security
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("success", false);
            return ResponseEntity.status(401).body(error);
        }
        
        User user = userOpt.get();
        
        // For demo: simple password check (in production, use BCrypt)
        boolean passwordMatches = "password".equals(password) || 
                                 passwordEncoder.matches(password, user.getPasswordHash());
        
        if (passwordMatches) {
            Map<String, Object> response = new HashMap<>();
            // Token format: jwt-token-{userId}-{timestamp} for easy extraction
            response.put("token", "jwt-token-" + user.getId() + "-" + System.currentTimeMillis());
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "role", "USER"
            ));
            response.put("message", "Login successful");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("success", false);
            return ResponseEntity.status(401).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userData) {
        String email = userData.get("email");
        String password = userData.get("password");
        String displayName = userData.get("displayName");
        
        if (email == null || password == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Email and password are required");
            return ResponseEntity.status(400).body(error);
        }
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "User with this email already exists");
            return ResponseEntity.status(409).body(error);
        }
        
        try {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setDisplayName(displayName != null ? displayName : email);
            
            User savedUser = userRepository.save(newUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "email", savedUser.getEmail(),
                "displayName", savedUser.getDisplayName()
            ));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token) {
        // In a real implementation, validate JWT token and return user info
        // For demo purposes, return mock data
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1L);
        user.put("email", "demo@example.com");
        user.put("displayName", "Demo User");
        user.put("role", "USER");
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // In production, you would:
            // 1. Extract token from Authorization header
            // 2. Add token to blacklist/revocation list
            // 3. Invalidate refresh token if using refresh tokens
            // 4. Clear any server-side session data
            
            // For demo purposes, just return success
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Logout failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}