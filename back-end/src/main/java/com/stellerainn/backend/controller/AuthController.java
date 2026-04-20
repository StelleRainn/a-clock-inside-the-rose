package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.User;
import com.stellerainn.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 (AuthController)
 * 负责用户的登录与注册逻辑。
 * 当前系统处于无状态或基于简易拦截器的认证模式。
 * 所有接口返回统一的 Result<T> 泛型封装对象，确保前端解析逻辑的一致性。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * @param user 包含 username 和 password 的用户实体（由前端 POST 的 JSON 序列化而来）
     * @return 成功则返回完整的脱敏 User 对象，失败则返回业务错误提示
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        // 调用 Service 层验证账密。如果存在则返回数据库中的 User 对象，否则返回 null
        User loggedInUser = userService.login(user.getUsername(), user.getPassword());
        if (loggedInUser != null) {
            // 安全防御：登录成功后，在将 User 对象返回给前端前，必须将密码置空。
            // 防止密码哈希值或明文在网络传输中被抓包泄漏。
            loggedInUser.setPassword(null); // Never return password
            return Result.success(loggedInUser);
        } else {
            return Result.error("Invalid username or password");
        }
    }

    /**
     * 用户注册接口
     * @param user 包含待注册用户名和密码的对象
     * @return 返回简单的字符串提示信息
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // Service 层的 register 可能会涉及唯一性校验（如用户名查重）
        boolean success = userService.register(user);
        if (success) {
            return Result.success("Registration successful");
        } else {
            // 如果用户名被占用或数据库插入异常，则返回失败结果
            return Result.error("Username already exists");
        }
    }
}
