package com.stellerainn.backend.service;

import com.stellerainn.backend.entity.PomodoroRecord;
import com.stellerainn.backend.mapper.PomodoroMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PomodoroService {

    @Autowired
    private PomodoroMapper pomodoroMapper;

    public PomodoroRecord saveRecord(PomodoroRecord record) {
        if (record.getStartTime() == null) {
            record.setStartTime(LocalDateTime.now().minusSeconds(record.getDurationSeconds()));
        }
        if (record.getEndTime() == null) {
            record.setEndTime(LocalDateTime.now());
        }
        pomodoroMapper.insert(record);
        return record;
    }

    public List<PomodoroRecord> getRecordsByUserId(Long userId) {
        return pomodoroMapper.findByUserId(userId);
    }

    public Integer getTodayCompletedCount(Long userId) {
        return pomodoroMapper.countTodayCompleted(userId);
    }
}
