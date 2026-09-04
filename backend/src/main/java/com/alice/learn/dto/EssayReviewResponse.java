package com.alice.learn.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EssayReviewResponse {

    private Long id;
    private BigDecimal overallScore;
    private BigDecimal taScore;
    private BigDecimal ccScore;
    private BigDecimal lrScore;
    private BigDecimal graScore;
    private String comment;
    private String sampleEssay;
    private List<FeedbackItem> feedback;
    private LocalDateTime createdAt;

    @Data
    public static class FeedbackItem {
        private String original;
        private String suggestion;
        private String reason;
    }
}
