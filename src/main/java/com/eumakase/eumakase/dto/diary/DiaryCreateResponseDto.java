package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
import lombok.*;

import java.io.Serializable;

/**
 * Diary 생성 응답 DTO
 */
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
public class DiaryCreateResponseDto implements Serializable {
    private Long id;
    private String emotion;
    private String content;

    public static DiaryCreateResponseDto of(Diary diary) {
        return DiaryCreateResponseDto.builder()
                .id(diary.getId())
                .emotion(diary.getPromptCategory().getMainPrompt())
                .content(diary.getContent())
                .build();
    }
}