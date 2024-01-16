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
    private String title;
    private String content;
    private String mood;

    public static DiaryCreateResponseDto of(Diary diary) {
        return DiaryCreateResponseDto.builder()
                .title(diary.getTitle())
                .content(diary.getContent())
                .mood(diary.getMood())
                .build();
    }
}