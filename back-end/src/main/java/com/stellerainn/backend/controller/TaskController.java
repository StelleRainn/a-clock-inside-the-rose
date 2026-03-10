package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.Task;
import com.stellerainn.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // TODO: In a real app, get userId from SecurityContext/Session
    // For now, we will pass userId as a request parameter or in the body for simplicity in this phase

    @GetMapping
    public Result<List<Task>> getTasks(@RequestParam Long userId) {
        return Result.success(taskService.getTasksByUserId(userId));
    }

    @PostMapping
    public Result<Task> createTask(@RequestBody Task task) {
        return Result.success(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    public Result<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.updateTask(task));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success("Deleted successfully");
    }

    @PostMapping("/reorder")
    public Result<String> reorderTasks(@RequestBody List<Long> taskIds) {
        taskService.updateTaskPositions(taskIds);
        return Result.success("Tasks reordered successfully");
    }
}
