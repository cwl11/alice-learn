package com.alice.learn.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotebookRequest {

    @NotNull
    private Long wordId;
}
