package com.alice.learn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EssaySaveRequest {

    @NotNull
    private Long taskId;

    @NotBlank
    private String content;

    private boolean submit;
}
