package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.DailyStats;
import com.stellerainn.backend.entity.TaskStats;
import com.stellerainn.backend.mapper.PomodoroMapper;
import com.stellerainn.backend.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    @Autowired
    private PomodoroMapper pomodoroMapper;

    @Autowired
    private TaskMapper taskMapper;

    public List<DailyStats> getDailyFocusStats(Long userId) {
        return pomodoroMapper.getDailyFocusTime(userId);
    }

    public List<TaskStats> getTaskStatusStats(Long userId) {
        return taskMapper.getTaskStatusStats(userId);
    }
}
