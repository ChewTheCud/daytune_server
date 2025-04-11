package com.eumakase.eumakase.dto.diary;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 일기 작성 시 질문-답변 DTO (최대 2개까지 입력)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswerDto {

    @NotBlank
    private String question;

    @NotBlank
    private String answer;
}
