package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.User;
import com.stellerainn.backend.mapper.UserMapper;
import com.stellerainn.backend.util.EncryptionUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            boolean passwordMatch = false;
            // Check if password is a BCrypt hash
            if (user.getPassword().startsWith("$2a$")) {
                passwordMatch = BCrypt.checkpw(password, user.getPassword());
            } else {
                // Fallback for existing plaintext passwords
                passwordMatch = user.getPassword().equals(password);
                // Upgrade to hashed password transparently
                if (passwordMatch) {
                    String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                    userMapper.updatePassword(user.getId(), hashed);
                    user.setPassword(hashed);
                }
            }

            if (passwordMatch) {
                // Decrypt API key before returning to frontend
                if (user.getGeminiApiKey() != null) {
                    user.setGeminiApiKey(EncryptionUtil.decrypt(user.getGeminiApiKey()));
                }
                return user;
            }
        }
        return null;
    }

    public boolean register(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return false; // User already exists
        }
        
        // Hash password before saving
        String plainPassword = user.getPassword();
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPassword(hashed);
        
        String plainKey = user.getGeminiApiKey();
        // Encrypt API Key if provided during registration (usually not)
        if (plainKey != null) {
            user.setGeminiApiKey(EncryptionUtil.encrypt(plainKey));
        }

        userMapper.insert(user);
        
        // Restore for object integrity in case it's used after
        user.setPassword(plainPassword);
        if (plainKey != null) {
            user.setGeminiApiKey(plainKey);
        }
        return true;
    }

    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user != null && user.getGeminiApiKey() != null) {
            user.setGeminiApiKey(EncryptionUtil.decrypt(user.getGeminiApiKey()));
        }
        return user;
    }

    public void updateProfile(User user) {
        // We shouldn't update password here unless intended. Assuming updateProfile doesn't update password.
        // Encrypt API key before saving
        String plainKey = user.getGeminiApiKey();
        if (plainKey != null) {
            user.setGeminiApiKey(EncryptionUtil.encrypt(plainKey));
        }
        userMapper.updateProfile(user);
        
        // Restore for object integrity
        if (plainKey != null) {
            user.setGeminiApiKey(plainKey);
        }
    }

    public void updatePassword(Long id, String rawPassword) {
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        userMapper.updatePassword(id, hashed);
    }
}
