package com.alice.learn.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCardRequest {

    @NotNull
    private Long wordId;

    @NotNull
    private Boolean remembered;
}
