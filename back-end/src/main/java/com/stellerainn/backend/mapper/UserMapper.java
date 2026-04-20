package com.stellerainn.backend.mapper;

import com.stellerainn.backend.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户持久层 (UserMapper)
 * 负责用户基本信息的 CRUD 操作。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户（用于登录或注册校验）
     * 亮点：展示了 MyBatis 中的下划线转驼峰映射。
     * 由于数据库中列名为 `gemini_api_key`，而 Java 实体中为 `geminiApiKey`，
     * 通过 @Results 和 @Result 注解实现了字段名的精确绑定。
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    @Results({
        @Result(property = "geminiApiKey", column = "gemini_api_key")
    })
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results({
        @Result(property = "geminiApiKey", column = "gemini_api_key")
    })
    User findById(Long id);

    @Update("UPDATE users SET nickname=#{nickname}, avatar_url=#{avatarUrl}, bio=#{bio}, " +
            "gender=#{gender}, website=#{website}, email=#{email}, gemini_api_key=#{geminiApiKey} WHERE id=#{id}")
    void updateProfile(User user);

    /**
     * 单独更新用户密码
     * @param id 用户主键
     * @param password 经过 BCrypt 哈希处理后的密文
     */
    @Update("UPDATE users SET password=#{password} WHERE id=#{id}")
    void updatePassword(@Param("id") Long id, @Param("password") String password);
}
