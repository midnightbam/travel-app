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
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No valid authorization token provided");
            return ResponseEntity.status(401).body(error);
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Extract user ID from token (format: jwt-token-{userId}-{timestamp})
            if (token.startsWith("jwt-token-") || token.startsWith("demo-jwt-token-")) {
                Long userId = extractUserIdFromToken(token);
                
                if (userId != null) {
                    // Handle demo user case
                    if (token.startsWith("demo-jwt-token-") && userId == 1L) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", 1L);
                        user.put("email", "demo@example.com");
                        user.put("displayName", "Demo User");
                        user.put("role", "USER");
                        user.put("createdAt", "2024-01-01T00:00:00");
                        user.put("totalTrips", 5);
                        return ResponseEntity.ok(user);
                    }
                    
                    // Try to find real user by ID
                    Optional<User> userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("email", user.getEmail());
                        userInfo.put("name", user.getName());
                        userInfo.put("displayName", user.getDisplayName());
                        userInfo.put("profileImage", user.getProfileImage());
                        userInfo.put("role", "USER");
                        userInfo.put("createdAt", user.getCreatedAt());
                        
                        // Get trip count for this user (assuming we have trips)
                        // For now, set to 0, can be enhanced later
                        userInfo.put("totalTrips", 0);
                        
                        return ResponseEntity.ok(userInfo);
                    }
                }
            }
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            return ResponseEntity.status(401).body(error);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token validation failed");
            return ResponseEntity.status(401).body(error);
        }
    }
    
    private Long extractUserIdFromToken(String token) {
        try {
            if (token.startsWith("demo-jwt-token-")) {
                return 1L; // Demo user always has ID 1
            } else if (token.startsWith("jwt-token-")) {
                // Extract from format: jwt-token-{userId}-{timestamp}
                String[] parts = token.split("-");
                if (parts.length >= 3) {
                    return Long.parseLong(parts[2]);
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
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
    
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> updates,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No valid authorization token provided");
            return ResponseEntity.status(401).body(error);
        }
        
        String token = authHeader.substring(7);
        
        try {
            Long userId = extractUserIdFromToken(token);
            
            if (userId != null) {
                // Handle demo user case
                if (token.startsWith("demo-jwt-token-") && userId == 1L) {
                    // For demo user, just return updated profile (can't persist)
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("id", 1L);
                    userProfile.put("email", "demo@example.com");
                    userProfile.put("name", updates.getOrDefault("name", "Demo User"));
                    userProfile.put("createdAt", "2024-01-01T00:00:00");
                    userProfile.put("profileImage", updates.getOrDefault("profileImage", ""));
                    return ResponseEntity.ok(userProfile);
                }
                
                // Update real user
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Update allowed fields
                    if (updates.containsKey("name")) {
                        user.setName(updates.get("name"));
                    }
                    if (updates.containsKey("displayName")) {
                        user.setDisplayName(updates.get("displayName"));
                    }
                    if (updates.containsKey("email")) {
                        // Check if email is already taken
                        if (!updates.get("email").equals(user.getEmail()) && 
                            userRepository.existsByEmail(updates.get("email"))) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("error", "Email already in use");
                            return ResponseEntity.status(400).body(error);
                        }
                        user.setEmail(updates.get("email"));
                    }
                    if (updates.containsKey("profileImage")) {
                        user.setProfileImage(updates.get("profileImage"));
                    }
                    
                    User updatedUser = userRepository.save(user);
                    
                    Map<String, Object> userProfile = new HashMap<>();
                    userProfile.put("id", updatedUser.getId());
                    userProfile.put("name", updatedUser.getName());
                    userProfile.put("displayName", updatedUser.getDisplayName());
                    userProfile.put("email", updatedUser.getEmail());
                    userProfile.put("createdAt", updatedUser.getCreatedAt());
                    userProfile.put("profileImage", updatedUser.getProfileImage());
                    
                    return ResponseEntity.ok(userProfile);
                }
            }
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            return ResponseEntity.status(401).body(error);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to update profile");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> passwordData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No valid authorization token provided");
            return ResponseEntity.status(401).body(error);
        }
        
        String token = authHeader.substring(7);
        
        try {
            Long userId = extractUserIdFromToken(token);
            
            if (userId != null) {
                // Handle demo user case
                if (token.startsWith("demo-jwt-token-") && userId == 1L) {
                    // For demo user, just return success (can't actually change password)
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Password changed successfully");
                    return ResponseEntity.ok(response);
                }
                
                // Change password for real user
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    String currentPassword = passwordData.get("currentPassword");
                    String newPassword = passwordData.get("newPassword");
                    
                    if (currentPassword == null || newPassword == null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("error", "Current password and new password are required");
                        return ResponseEntity.status(400).body(error);
                    }
                    
                    // Verify current password
                    boolean currentPasswordMatches = "password".equals(currentPassword) || 
                                                   passwordEncoder.matches(currentPassword, user.getPasswordHash());
                    
                    if (!currentPasswordMatches) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("error", "Current password is incorrect");
                        return ResponseEntity.status(400).body(error);
                    }
                    
                    // Validate new password (basic validation)
                    if (newPassword.length() < 8) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("error", "New password must be at least 8 characters long");
                        return ResponseEntity.status(400).body(error);
                    }
                    
                    // Update password
                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Password changed successfully");
                    return ResponseEntity.ok(response);
                }
            }
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            return ResponseEntity.status(401).body(error);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to change password");
            return ResponseEntity.status(500).body(error);
        }
    }
}