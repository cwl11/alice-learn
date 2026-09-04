package com.alice.learn.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReadingPassageResponse {

    private Long id;
    private String title;
    private String content;
    private String difficulty;
    private List<ReadingQuestionResponse> questions;
}
