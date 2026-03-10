package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.Subtask;
import com.stellerainn.backend.entity.Tag;
import com.stellerainn.backend.entity.Task;
import com.stellerainn.backend.mapper.SubtaskMapper;
import com.stellerainn.backend.mapper.TagMapper;
import com.stellerainn.backend.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private SubtaskMapper subtaskMapper;

    public List<Task> getTasksByUserId(Long userId) {
        List<Task> tasks = taskMapper.findByUserId(userId);
        for (Task task : tasks) {
            task.setTags(tagMapper.findTagsByTaskId(task.getId()));
            task.setSubtasks(subtaskMapper.findByTaskId(task.getId()));
        }
        return tasks;
    }

    @Transactional
    public Task createTask(Task task) {
        taskMapper.insert(task);
        updateTaskTags(task);
        // Subtasks are usually added after task creation or as a separate step, 
        // but if provided here, we could save them. 
        // For simplicity, we'll stick to basic creation first.
        return task;
    }

    @Transactional
    public Task updateTask(Task task) {
        taskMapper.update(task);
        updateTaskTags(task);
        Task updatedTask = taskMapper.findById(task.getId());
        updatedTask.setTags(tagMapper.findTagsByTaskId(task.getId()));
        updatedTask.setSubtasks(subtaskMapper.findByTaskId(task.getId()));
        return updatedTask;
    }

    private void updateTaskTags(Task task) {
        if (task.getTags() != null) {
            tagMapper.removeAllTaskTags(task.getId());
            for (Tag tag : task.getTags()) {
                tagMapper.addTaskTag(task.getId(), tag.getId());
            }
        }
    }

    @Transactional
    public void updateTaskPositions(List<Long> taskIds) {
        for (int i = 0; i < taskIds.size(); i++) {
            taskMapper.updatePosition(taskIds.get(i), i);
        }
    }

    public void deleteTask(Long id) {
        taskMapper.delete(id);
    }
    
    public Task getTaskById(Long id) {
        Task task = taskMapper.findById(id);
        if (task != null) {
            task.setTags(tagMapper.findTagsByTaskId(id));
            task.setSubtasks(subtaskMapper.findByTaskId(id));
        }
        return task;
    }

    // Subtask Methods
    public Subtask addSubtask(Subtask subtask) {
        subtaskMapper.insert(subtask);
        return subtask;
    }

    public void updateSubtask(Subtask subtask) {
        subtaskMapper.update(subtask);
    }

    public void deleteSubtask(Long id) {
        subtaskMapper.delete(id);
    }
}
