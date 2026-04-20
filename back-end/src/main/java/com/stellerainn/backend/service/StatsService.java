package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.DailyStats;
import com.stellerainn.backend.entity.TagFocusStats;
import com.stellerainn.backend.entity.TaskStats;
import com.stellerainn.backend.mapper.PomodoroMapper;
import com.stellerainn.backend.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 统计聚合服务层 (StatsService)
 * 这是一个典型的 Facade (门面) 服务，它自己不包含复杂的计算逻辑，
 * 而是将计算压力下推到持久层 (PomodoroMapper, TaskMapper) 的高阶 SQL 中。
 * 它的职责是将底层聚合并格式化好的二维统计数组透传给 Controller，供前端 ECharts 直接渲染。
 */
@Service
public class StatsService {

    @Autowired
    private PomodoroMapper pomodoroMapper;

    @Autowired
    private TaskMapper taskMapper;

    /**
     * 获取每日专注时长统计（用于 GitHub 风格年度热力图）
     */
    public List<DailyStats> getDailyFocusStats(Long userId) {
        return pomodoroMapper.getDailyFocusTime(userId);
    }

    /**
     * 获取任务状态分布统计（待办、进行中、已完成等）
     */
    public List<TaskStats> getTaskStatusStats(Long userId) {
        return taskMapper.getTaskStatusStats(userId);
    }

    /**
     * 获取各个标签下的专注时长统计（用于南丁格尔玫瑰图）
     */
    public List<TagFocusStats> getTagFocusStats(Long userId) {
        return pomodoroMapper.getTagFocusStats(userId);
    }
}
