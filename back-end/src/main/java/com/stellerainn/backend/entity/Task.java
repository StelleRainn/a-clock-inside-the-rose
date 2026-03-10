package com.stellerainn.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Task {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String status; // TODO, IN_PROGRESS, DONE
    private String priority; // LOW, MEDIUM, HIGH
    private Integer position; // Manual ordering
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relations
    private List<Tag> tags;
    private List<Subtask> subtasks;
}
