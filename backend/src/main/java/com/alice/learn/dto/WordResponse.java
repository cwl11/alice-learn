package com.alice.learn.dto;

import lombok.Data;

@Data
public class WordResponse {

    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    private String example;
    private String category;
    private boolean inNotebook;
    private Integer familiarity;
}
