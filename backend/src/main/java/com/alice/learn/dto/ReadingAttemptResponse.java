package com.alice.learn.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReadingAttemptResponse {

    private Long id;
    private Long passageId;
    private String passageTitle;
    private String difficulty;
    private Integer total;
    private Integer correctCount;
    private Integer timeSpentSec;
    private LocalDateTime createdAt;
    /** 仅详情接口返回 */
    private List<SubmitAnswersResponse.Item> items;
}
