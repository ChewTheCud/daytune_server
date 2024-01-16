package com.eumakase.eumakase.dto.diary;

import com.eumakase.eumakase.domain.Diary;
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

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String mood;

    public Diary toEntity(final DiaryCreateRequestDto diaryCreateRequestDto) {
        return Diary.builder()
                .title(diaryCreateRequestDto.getTitle())
                .content(diaryCreateRequestDto.getContent())
                .mood(diaryCreateRequestDto.getMood())
                .build();
    }
}