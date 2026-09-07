package com.alice.learn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswersRequest {

    @NotNull
    private Long passageId;

    /** 作答用时（秒），可选 */
    @Min(0)
    private Integer timeSpentSec;

    @NotEmpty
    @Valid
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull
        private Long questionId;

        /** 未作答传空串即可 */
        @NotNull
        @Size(max = 200)
        private String answer;
    }
}
