package com.alice.learn.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AiReviewResult {

    private BigDecimal taScore;
    private BigDecimal ccScore;
    private BigDecimal lrScore;
    private BigDecimal graScore;
    private String comment;
    private String sampleEssay;
    private List<EssayReviewResponse.FeedbackItem> feedback;
}
