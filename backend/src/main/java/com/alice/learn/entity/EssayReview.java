package com.alice.learn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("essay_review")
public class EssayReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long essayId;
    private BigDecimal overallScore;
    private BigDecimal taScore;
    private BigDecimal ccScore;
    private BigDecimal lrScore;
    private BigDecimal graScore;
    private String feedbackJson;
    private String sampleEssay;
    private String comment;
    private LocalDateTime createdAt;
}
