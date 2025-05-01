package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.util.enums.MainEmotion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 알가 감정분석 결과 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class EmotionInsightRequestDto implements Serializable {
    @NotNull(message = "오늘의 메인 감정은 필수입니다.")
    private MainEmotion mainEmotion; // "positive", "neutral", "negative" 중 하나

    @Valid
    @Size(min = 1, max = 2, message = "질문-답변 쌍은 1개 이상 2개 이하로 입력해야 합니다.")
    private List<QuestionAnswerDto> questionAnswers;
}
