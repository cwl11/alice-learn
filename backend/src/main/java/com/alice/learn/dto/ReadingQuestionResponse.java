package com.alice.learn.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReadingQuestionResponse {

    private Long id;
    private String questionType;
    private String question;
    private List<String> options;
    private String answer;
    private String explanation;
}
