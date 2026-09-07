package com.alice.learn.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswersResponse {

    private Long attemptId;
    private int total;
    private int correctCount;
    private List<Item> items;

    @Data
    public static class Item {
        private Long questionId;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
        private String explanation;
    }
}
