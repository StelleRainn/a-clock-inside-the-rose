package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.User;
import com.stellerainn.backend.mapper.UserMapper;
import com.stellerainn.backend.util.EncryptionUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务层 (UserService)
 * 处理用户的核心逻辑，包括登录注册验证、密码的哈希加密 (BCrypt)、
 * 以及 Gemini API Key 的对称加密与解密。
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录逻辑
     * 亮点：不仅包含密码校验，还实现了“明文密码的无感升级”。
     */
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            boolean passwordMatch = false;
            // 检查密码是否已经是 BCrypt 的哈希格式（以 $2a$ 开头）
            if (user.getPassword().startsWith("$2a$")) {
                passwordMatch = BCrypt.checkpw(password, user.getPassword());
            } else {
                // 如果是老版本系统遗留的明文密码，进行明文比对
                passwordMatch = user.getPassword().equals(password);
                // 无感升级机制：一旦明文密码比对成功，立刻在后台使用 BCrypt 进行哈希处理并更新数据库。
                // 这样下次该用户登录时就会走安全的 BCrypt 校验逻辑。
                if (passwordMatch) {
                    String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                    userMapper.updatePassword(user.getId(), hashed);
                    user.setPassword(hashed);
                }
            }

            if (passwordMatch) {
                // API Key 属于高度敏感信息，在数据库中是加密存储的。
                // 登录成功返回给前端前，需要使用对称加密算法解密，以便前端携带其请求大模型。
                if (user.getGeminiApiKey() != null) {
                    user.setGeminiApiKey(EncryptionUtil.decrypt(user.getGeminiApiKey()));
                }
                return user;
            }
        }
        return null;
    }

    /**
     * 用户注册逻辑
     */
    public boolean register(User user) {
        // 防止用户名重复
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return false; // User already exists
        }
        
        // 注册时必须对明文密码进行不可逆的 BCrypt 哈希处理
        String plainPassword = user.getPassword();
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        user.setPassword(hashed);
        
        String plainKey = user.getGeminiApiKey();
        // 如果注册时一并提交了 API Key（一般在 Profile 页面提交），也必须先加密再入库
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
        // 查询出用户信息时，务必将 API Key 解密后才可使用或返回
        if (user != null && user.getGeminiApiKey() != null) {
            user.setGeminiApiKey(EncryptionUtil.decrypt(user.getGeminiApiKey()));
        }
        return user;
    }

    /**
     * 更新用户资料 (如设置页面)
     */
    public void updateProfile(User user) {
        // We shouldn't update password here unless intended. Assuming updateProfile doesn't update password.
        // 敏感数据入库前必须加密
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
