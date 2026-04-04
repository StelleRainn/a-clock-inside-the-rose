package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.User;
import com.stellerainn.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setPassword(null); // Don't return password
            return Result.success(user);
        }
        return Result.error("User not found");
    }

    @PutMapping("/{id}")
    public Result<User> updateProfile(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return Result.error("User not found");
        }
        
        // Only update allowed fields
        existingUser.setNickname(user.getNickname());
        existingUser.setAvatarUrl(user.getAvatarUrl());
        existingUser.setBio(user.getBio());
        existingUser.setGender(user.getGender());
        existingUser.setWebsite(user.getWebsite());
        
        // Update Gemini API Key if provided
        if (user.getGeminiApiKey() != null) {
            existingUser.setGeminiApiKey(user.getGeminiApiKey());
        }
        
        // Update password if provided
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            userService.updatePassword(id, user.getPassword());
        }
        
        userService.updateProfile(existingUser);
        existingUser.setPassword(null);
        return Result.success(existingUser);
    }
}
