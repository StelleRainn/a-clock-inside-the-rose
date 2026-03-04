package com.stellerainn.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PomodoroRecord {
    private Long id;
    private Long userId;
    private Long taskId; // Optional
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private String status; // COMPLETED, INTERRUPTED
    private LocalDateTime createdAt;
}
