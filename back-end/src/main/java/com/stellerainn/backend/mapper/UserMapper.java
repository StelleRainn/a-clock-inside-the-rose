package com.stellerainn.backend.mapper;

import com.stellerainn.backend.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

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

    @Update("UPDATE users SET password=#{password} WHERE id=#{id}")
    void updatePassword(@Param("id") Long id, @Param("password") String password);
}
