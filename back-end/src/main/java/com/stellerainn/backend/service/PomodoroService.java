package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.PomodoroRecord;
import com.stellerainn.backend.mapper.PomodoroMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 番茄钟/专注服务层 (PomodoroService)
 * 负责记录用户的专注时长，并与游戏化统计系统 (UserStatsService) 联动。
 */
@Service
public class PomodoroService {

    @Autowired
    private PomodoroMapper pomodoroMapper;

    @Autowired
    private UserStatsService userStatsService;

    /**
     * 保存专注记录
     * 亮点：这是一个典型的“领域事件触发”场景。当一个番茄钟完成时，不仅要记流水账，还要增加用户的经验值。
     */
    public PomodoroRecord saveRecord(PomodoroRecord record) {
        // 健壮性处理：如果前端没有传开始时间，则根据持续时间倒推
        if (record.getStartTime() == null) {
            record.setStartTime(LocalDateTime.now().minusSeconds(record.getDurationSeconds()));
        }
        if (record.getEndTime() == null) {
            record.setEndTime(LocalDateTime.now());
        }
        // 1. 插入专注明细流水
        pomodoroMapper.insert(record);

        // 2. 更新用户维度的统计数据（游戏化：增加总专注时长，可能会触发升级逻辑）
        // Update User Stats (Gamification)
        if ("COMPLETED".equals(record.getStatus())) {
            userStatsService.addFocusTime(record.getUserId(), record.getDurationSeconds());
        }

        return record;
    }

    public List<PomodoroRecord> getRecordsByUserId(Long userId) {
        return pomodoroMapper.findByUserId(userId);
    }

    public Integer getTodayCompletedCount(Long userId) {
        return pomodoroMapper.countTodayCompleted(userId);
    }

    public Long getTodayFocusSeconds(Long userId) {
        return pomodoroMapper.sumTodayDurationSeconds(userId);
    }
}
