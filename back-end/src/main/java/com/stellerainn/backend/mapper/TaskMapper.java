package com.stellerainn.backend.mapper;

import com.stellerainn.backend.entity.Task;
import com.stellerainn.backend.entity.TaskStats;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 任务持久层 (TaskMapper)
 * 基于 MyBatis 注解方式直接在接口方法上编写 SQL，省去了冗长的 XML 映射文件。
 */
@Mapper
public interface TaskMapper {

    /**
     * 查询用户任务列表
     * 亮点：通过 ORDER BY position ASC, created_at DESC 保证了任务的有序性。
     * 前端看板的拖拽顺序能够被完美持久化和还原。
     */
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} ORDER BY position ASC, created_at DESC")
    List<Task> findByUserId(Long userId);

    @Select("SELECT * FROM tasks WHERE id = #{id}")
    Task findById(Long id);

    /**
     * 插入新任务
     * 亮点：使用了 @Options(useGeneratedKeys = true, keyProperty = "id")
     * 这意味着 MySQL 自动生成的自增 ID 会被 MyBatis 自动反写回传入的 Task 实体对象中。
     */
    @Insert("INSERT INTO tasks(user_id, title, description, status, priority, due_date, position) " +
            "VALUES(#{userId}, #{title}, #{description}, #{status}, #{priority}, #{dueDate}, #{position})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Task task);

    @Update("UPDATE tasks SET title=#{title}, description=#{description}, status=#{status}, " +
            "priority=#{priority}, due_date=#{dueDate}, position=#{position} WHERE id=#{id}")
    void update(Task task);

    /**
     * 挤占式重排的核心原子操作
     * 在 Service 层通过循环和事务调用该方法，只更新任务的位置字段。
     */
    @Update("UPDATE tasks SET position = #{position} WHERE id = #{id}")
    void updatePosition(@Param("id") Long id, @Param("position") Integer position);

    @Delete("DELETE FROM tasks WHERE id = #{id}")
    void delete(Long id);

    /**
     * 任务状态分布聚合
     * 直接在数据库层使用 GROUP BY status 算出每种状态下的任务数量，提供给 ECharts 渲染饼图。
     */
    @Select("SELECT status, COUNT(*) as count FROM tasks WHERE user_id = #{userId} GROUP BY status")
    List<TaskStats> getTaskStatusStats(Long userId);
}
