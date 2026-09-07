package com.alice.learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EssayUpdateRequest {

    @NotBlank
    private String content;

    private boolean submit;
}
