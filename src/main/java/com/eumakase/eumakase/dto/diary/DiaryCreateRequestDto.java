package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.PromptCategoryRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

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
    private String emotion;

    @NotBlank
    private String content;

    @NotBlank
    private String prompt;

    public Diary toEntity(final DiaryCreateRequestDto diaryCreateRequestDto, User user, PromptCategory promptCategory) {
        return Diary.builder()
                .user(user)
                .content(diaryCreateRequestDto.getContent())
                .promptCategory(promptCategory)
                .prompt(diaryCreateRequestDto.getPrompt()) // Modify this line
                .build();
    }
}