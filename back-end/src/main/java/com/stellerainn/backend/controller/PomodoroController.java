package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.PomodoroRecord;
import com.stellerainn.backend.service.PomodoroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pomodoro")
public class PomodoroController {

    @Autowired
    private PomodoroService pomodoroService;

    @PostMapping
    public Result<PomodoroRecord> saveRecord(@RequestBody PomodoroRecord record) {
        return Result.success(pomodoroService.saveRecord(record));
    }

    @GetMapping
    public Result<List<PomodoroRecord>> getRecords(@RequestParam Long userId) {
        return Result.success(pomodoroService.getRecordsByUserId(userId));
    }

    @GetMapping("/today-count")
    public Result<Map<String, Object>> getTodayStats(@RequestParam Long userId) {
        Integer count = pomodoroService.getTodayCompletedCount(userId);
        Long seconds = pomodoroService.getTodayFocusSeconds(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("count", count);
        stats.put("totalSeconds", seconds);
        
        return Result.success(stats);
    }
}
