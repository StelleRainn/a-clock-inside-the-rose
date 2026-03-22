package com.stellerainn.backend.entity;

import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    
    @ToString.Exclude
    private String password;
    
    private String email;
    // New Profile Fields
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String gender; // MALE, FEMALE, OTHER
    private String website;
    
    // AI Integration
    @ToString.Exclude
    private String geminiApiKey;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
