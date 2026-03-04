package com.stellerainn.backend.entity;

import lombok.Data;

@Data
public class DailyStats {
    private String date; // YYYY-MM-DD
    private Integer totalSeconds;
}
