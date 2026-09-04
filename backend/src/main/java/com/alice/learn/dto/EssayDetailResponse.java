package com.alice.learn.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EssayDetailResponse {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private String taskType;
    private String taskDescription;
    private String content;
    private Integer wordCount;
    private String status;
    private LocalDateTime createdAt;
    private EssayReviewResponse review;
}
