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
    private String mood;

    public static DiaryReadResponseDto of(Diary diary) {
        return DiaryReadResponseDto.builder()
                .id(diary.getId())
                //.userId(diary.getUser() != null ? diary.getUser().getId() : null) // TODO: 24.01.17 로그인 기능 구현 후 반영
                .content(diary.getContent())
                .mood(diary.getMood())
                .build();
    }
}