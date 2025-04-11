package com.eumakase.eumakase.dto.diary;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 일기 작성 시 질문-답변 DTO (최대 2개까지 입력)
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class QuestionAnswerDto {

    @Min(1)
    @Max(2)
    private int order;

    @NotBlank
    private String question;

    @NotBlank
    private String answer;
}
