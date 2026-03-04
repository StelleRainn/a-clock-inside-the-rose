package com.stellerainn.backend.controller;

import com.stellerainn.backend.common.Result;
import com.stellerainn.backend.entity.DailyStats;
import com.stellerainn.backend.entity.TaskStats;
import com.stellerainn.backend.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/focus-daily")
    public Result<List<DailyStats>> getDailyFocusStats(@RequestParam Long userId) {
        return Result.success(statsService.getDailyFocusStats(userId));
    }

    @GetMapping("/task-status")
    public Result<List<TaskStats>> getTaskStatusStats(@RequestParam Long userId) {
        return Result.success(statsService.getTaskStatusStats(userId));
    }
}
