package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.Task;
import com.stellerainn.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器 (TaskController)
 * 提供对任务列表的增删改查 (CRUD) 以及任务排序等核心业务接口。
 * 遵循标准的 RESTful API 设计规范：
 * - GET 获取资源
 * - POST 创建资源 / 执行特定动作（如 reorder）
 * - PUT 更新资源
 * - DELETE 删除资源
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // TODO: In a real app, get userId from SecurityContext/Session
    // For now, we will pass userId as a request parameter or in the body for simplicity in this phase

    /**
     * 获取指定用户的所有任务
     * @param userId 通过 URL 的 query parameter 传入 (?userId=xxx)
     * @return 返回包含 Task 列表的 Result 统一包装对象
     */
    @GetMapping
    public Result<List<Task>> getTasks(@RequestParam Long userId) {
        return Result.success(taskService.getTasksByUserId(userId));
    }

    /**
     * 创建新任务
     * @param task 由前端传入的 JSON 数据通过 Spring 的 Jackson 转换成 Task 对象
     * @return 返回创建成功后包含主键 ID 的完整 Task 实体
     */
    @PostMapping
    public Result<Task> createTask(@RequestBody Task task) {
        return Result.success(taskService.createTask(task));
    }

    /**
     * 更新指定任务
     * @param id URL 路径变量 (PathVariable)，表示被修改的任务 ID
     * @param task 包含待修改属性的 JSON 体
     * @return 返回更新后的任务实体
     */
    @PutMapping("/{id}")
    public Result<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        // 安全策略：强制使用路径中提取的 ID，防止 JSON 体中传入伪造的 ID 导致数据错乱
        task.setId(id);
        return Result.success(taskService.updateTask(task));
    }

    /**
     * 删除指定任务
     * @param id 任务 ID
     * @return 操作成功的字符串提示
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTask(@PathVariable Long id) {
        // Service 层会处理该任务以及级联关联的数据（如它的子任务）
        taskService.deleteTask(id);
        return Result.success("Deleted successfully");
    }

    /**
     * 重新排序任务列表（如响应前端的拖拽操作）
     * @param taskIds 按新的顺序排列的任务 ID 列表
     * @return 成功信息
     */
    @PostMapping("/reorder")
    public Result<String> reorderTasks(@RequestBody List<Long> taskIds) {
        // 调用 Service 层批量更新任务的 position 或 order 字段
        taskService.updateTaskPositions(taskIds);
        return Result.success("Tasks reordered successfully");
    }
}
