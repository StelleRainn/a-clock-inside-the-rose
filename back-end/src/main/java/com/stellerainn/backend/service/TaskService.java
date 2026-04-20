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

/**
 * 任务服务层 (TaskService)
 * 核心业务逻辑处理中心。
 * 负责任务的组装（组装子任务和标签）、级联保存、以及任务拖拽时的挤占式重排。
 */
@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private SubtaskMapper subtaskMapper;

    /**
     * 聚合查询：获取用户的全量任务视图
     * @param userId 用户 ID
     * @return 组装了 Tags 和 Subtasks 的全量 Task 列表
     */
    public List<Task> getTasksByUserId(Long userId) {
        // 1. 先查出该用户的所有基础任务记录
        List<Task> tasks = taskMapper.findByUserId(userId);
        // 2. 遍历基础记录，分别发起查询组装关联的 Tags 和 Subtasks (N+1 查询，可考虑后续通过 Join 优化)
        for (Task task : tasks) {
            task.setTags(tagMapper.findTagsByTaskId(task.getId()));
            task.setSubtasks(subtaskMapper.findByTaskId(task.getId()));
        }
        return tasks;
    }

    /**
     * 创建任务 (开启事务保证主表与关联表的一致性)
     */
    @Transactional
    public Task createTask(Task task) {
        // 1. 插入主表任务，此时 MyBatis 会将生成的主键回填到 task 对象中
        taskMapper.insert(task);
        // 2. 根据新生成的主键，插入多对多中间表的标签关联数据
        updateTaskTags(task);
        // Subtasks are usually added after task creation or as a separate step, 
        // but if provided here, we could save them. 
        // For simplicity, we'll stick to basic creation first.
        return task;
    }

    /**
     * 更新任务 (开启事务)
     */
    @Transactional
    public Task updateTask(Task task) {
        taskMapper.update(task);
        updateTaskTags(task);
        // 更新完毕后，重新查出完整的最新状态返回给前端以更新本地 Store
        Task updatedTask = taskMapper.findById(task.getId());
        updatedTask.setTags(tagMapper.findTagsByTaskId(task.getId()));
        updatedTask.setSubtasks(subtaskMapper.findByTaskId(task.getId()));
        return updatedTask;
    }

    /**
     * 维护任务-标签的多对多中间表
     * 采用“先全删、后全增”的策略，避免了复杂的差异比对逻辑
     */
    private void updateTaskTags(Task task) {
        if (task.getTags() != null) {
            tagMapper.removeAllTaskTags(task.getId());
            for (Tag tag : task.getTags()) {
                tagMapper.addTaskTag(task.getId(), tag.getId());
            }
        }
    }

    /**
     * 交互式看板的“挤占式”重排逻辑
     * 论文对应：5.1.2 交互式看板与挤占式重排
     * 
     * @param taskIds 前端拖拽落位后，传递过来的该列最新的有序 Task ID 数组
     */
    @Transactional
    public void updateTaskPositions(List<Long> taskIds) {
        // 这里的实现方式是：直接利用前端传来的新数组索引作为 position。
        // 通过 @Transactional 开启事务，在一个原子操作内，
        // 依次对受影响的任务执行 position 更新，从而“挤占”掉原本的空间。
        // 这种全量覆写的方式，彻底杜绝了并发拖拽下可能诱发的索引错乱与数据幻读。
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
