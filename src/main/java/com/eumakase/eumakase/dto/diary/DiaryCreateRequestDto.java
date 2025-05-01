package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.PromptCategoryRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * Diary 생성 요청 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class DiaryCreateRequestDto implements Serializable {
    private PromptCategoryRepository promptCategoryRepository;

    @NotBlank
    private String mainEmotion;

    @NotEmpty
    private List<QuestionAnswerDto> questionAnswers;  // 질문-답변 2쌍

    @NotEmpty
    private List<EmotionInsightDto> emotions;  // 감정/분석내용 (2~3개)

    public Diary toEntity(User user, PromptCategory promptCategory) {
        return Diary.builder()
                .user(user)
                .promptCategory(promptCategory)
                .build();
    }
}