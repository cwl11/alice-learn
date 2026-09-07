package com.alice.learn.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EssaySummaryResponse {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private String taskType;
    private Integer wordCount;
    private String status;
    private BigDecimal overallScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
