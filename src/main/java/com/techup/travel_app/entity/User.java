package com.techup.travel_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "display_name", length = 100)
    private String displayName;
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructor for backward compatibility
    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }
    
    // Full constructor
    public User(String email, String passwordHash, String displayName, String name, String profileImage) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.name = name;
        this.profileImage = profileImage;
    }
}