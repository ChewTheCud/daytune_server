package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String content;

    @NotBlank
    private String prompt;

    public Diary toEntity(final DiaryCreateRequestDto diaryCreateRequestDto, User user) {
        return Diary.builder()
                .user(user)
                .content(diaryCreateRequestDto.getContent())
                .prompt(diaryCreateRequestDto.getPrompt()) // Modify this line
                .build();
    }
}