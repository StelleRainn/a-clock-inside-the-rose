package com.stellerainn.backend.mapper;

import com.stellerainn.backend.entity.Task;
import com.stellerainn.backend.entity.TaskStats;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {

    @Select("SELECT * FROM tasks WHERE user_id = #{userId}")
    List<Task> findByUserId(Long userId);

    @Select("SELECT * FROM tasks WHERE id = #{id}")
    Task findById(Long id);

    @Insert("INSERT INTO tasks(user_id, title, description, status, priority, due_date) " +
            "VALUES(#{userId}, #{title}, #{description}, #{status}, #{priority}, #{dueDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Task task);

    @Update("UPDATE tasks SET title=#{title}, description=#{description}, status=#{status}, " +
            "priority=#{priority}, due_date=#{dueDate} WHERE id=#{id}")
    void update(Task task);

    @Delete("DELETE FROM tasks WHERE id = #{id}")
    void delete(Long id);

    @Select("SELECT status, COUNT(*) as count FROM tasks WHERE user_id = #{userId} GROUP BY status")
    List<TaskStats> getTaskStatusStats(Long userId);
}
