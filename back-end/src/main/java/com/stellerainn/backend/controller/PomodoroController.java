package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.PomodoroRecord;
import com.stellerainn.backend.service.PomodoroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<Integer> getTodayCount(@RequestParam Long userId) {
        return Result.success(pomodoroService.getTodayCompletedCount(userId));
    }
}
