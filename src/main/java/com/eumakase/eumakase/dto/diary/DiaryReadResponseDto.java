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
public class DiaryReadResponseDto implements Serializable {
    private Long id;
    private Long userId;
    private String content;

    public static DiaryReadResponseDto of(Diary diary) {
        return DiaryReadResponseDto.builder()
                .id(diary.getId())
                .userId(diary.getUser() != null ? diary.getUser().getId() : null)
                .content(diary.getContent())
                .build();
    }
}